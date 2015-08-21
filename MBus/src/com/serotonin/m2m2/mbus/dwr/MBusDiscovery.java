/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus.dwr;

import java.io.IOException;
import java.util.Map;

import net.sf.mbus4j.Connection;
import net.sf.mbus4j.MBusAddressing;
import net.sf.mbus4j.TcpIpConnection;
import net.sf.mbus4j.dataframes.MBusResponseFramesContainer;
import net.sf.mbus4j.master.MBusMaster;
import net.sf.mbus4j.master.MasterEventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.mbus.MangoMBusSerialConnection;
import com.serotonin.m2m2.web.dwr.beans.AutoShutOff;
import com.serotonin.m2m2.web.dwr.beans.TestingUtility;

/**
 * @author aploese
 */
public class MBusDiscovery implements MasterEventListener, TestingUtility {
    public static MBusDiscovery createPrimaryAddressingSearch(Translations translations, String commPortId,
            String phonenumber, int baudrate, int flowControlIn, int flowcontrolOut, int dataBits, int stopBits,
            int parity, int firstPrimaryAddress, int lastPrimaryAddress, int responseTimeoutOffset) {
        MBusDiscovery result = new MBusDiscovery(translations, commPortId, phonenumber, MBusAddressing.PRIMARY,
                baudrate, flowControlIn, flowcontrolOut, dataBits, stopBits, parity, responseTimeoutOffset);
        result.firstPrimaryAddress = firstPrimaryAddress;
        result.lastPrimaryAddress = lastPrimaryAddress;
        result.searchThread.start();
        return result;
    }

    public static MBusDiscovery createPrimaryAddressingSearch(Translations translations, String host,
            int port, int firstPrimaryAddress, int lastPrimaryAddress, int responseTimeoutOffset) {
        MBusDiscovery result = new MBusDiscovery(translations, host, port, MBusAddressing.PRIMARY,
                responseTimeoutOffset);
        result.firstPrimaryAddress = firstPrimaryAddress;
        result.lastPrimaryAddress = lastPrimaryAddress;
        result.searchThread.start();
        return result;
    }
    
    public static MBusDiscovery createSecondaryAddressingSearch(Translations translations, String commPortId,
            String phonenumber, int baudrate, int flowControlIn, int flowcontrolOut, int dataBits, int stopBits,
            int parity, int responseTimeoutOffset) {
        MBusDiscovery result = new MBusDiscovery(translations, commPortId, phonenumber, MBusAddressing.SECONDARY,
                baudrate, flowControlIn, flowcontrolOut, dataBits, stopBits, parity, responseTimeoutOffset);
        result.searchThread.start();
        return result;
    }

    public MBusResponseFramesContainer getDevice(int deviceIndex) {
        return master.getDevice(deviceIndex);
    }

    class SearchThread extends Thread {
        @Override
        public void run() {
            LOG.info("start search");
            try {
                if (mBusAddressing == MBusAddressing.PRIMARY) {
                    master.searchDevicesByPrimaryAddress((byte)firstPrimaryAddress, (byte)lastPrimaryAddress);
                }
                else {
                	int tries = 2;
                    master.searchDevicesBySecondaryAddressing(tries);
                }
            }
            catch (InterruptedException ex) {
                LOG.info("Interrupted)");
            }
            catch (IOException ex) {
                LOG.warn("SearchThread.run", ex);
            }
            catch (Exception ex) {
                LOG.warn("SearchThread.run", ex);
            }
            LOG.info("Search finished!");
            try {
                finished = true;
                master.close();
            }
            catch (IOException ex) {
                LOG.info("Interrupted)");
            }
        }
    }

    static final Log LOG = LogFactory.getLog(MBusDiscovery.class);
    final Translations translations;
    // private final int removeDeviceIndex = 1;
    final MBusAddressing mBusAddressing;
    final MBusMaster master;
    // private String phonenumber;
    private final AutoShutOff autoShutOff;
    String message;
    boolean finished;
    private final SearchThread searchThread;

    int lastPrimaryAddress;
    int firstPrimaryAddress;

    /**
     * SERIAL_DIRECT connection
     * 
     * @param bundle
     * @param commPortId
     * @param mBusAddressing
     * @param baudrate
     * @param flowControlIn
     * @param flowcontrolOut
     * @param dataBits
     * @param stopBits
     * @param parity
     */
    private MBusDiscovery(Translations translations, String comPortId, String phonenumber,
            MBusAddressing mBusAddressing, int baudrate, int flowControlIn, int flowControlOut, int dataBits,
            int stopBits, int parity, int responseTimeoutOffset) {
        if ((phonenumber != null) && (phonenumber.length() > 0)) {
            throw new IllegalArgumentException("Modem with Phonenumber not implemented yet!");
        }
        LOG.info("MBusDiscovery(...)");
        this.translations = translations;

        autoShutOff = new AutoShutOff(AutoShutOff.DEFAULT_TIMEOUT * 4) {
            @Override
            public void shutOff() {
                message = MBusDiscovery.this.translations.translate("dsEdit.mbus.tester.autoShutOff");
                MBusDiscovery.this.cleanup();
            }
        };

        this.mBusAddressing = mBusAddressing;
        // Thread starten , der sucht....
        master = new MBusMaster();
        try {
			
			String owner = "Mango MBus Serial Test Tool by " + Common.getUser().getUsername();
			
			MangoMBusSerialConnection conn = new MangoMBusSerialConnection(
					owner,
					comPortId,
					baudrate,
					flowControlIn,
					flowControlOut,
					dataBits,
					stopBits,
					parity,
					responseTimeoutOffset);

            master.setConnection(conn);
            master.open();
        }
        catch (Exception ex) {
            // no op
        	 LOG.warn("MBusDiscovery(...)", ex);
        } 
        // TODO master init
        message = translations.translate("dsEdit.mbus.tester.searchingDevices");
        searchThread = new SearchThread();
    }

    public void addUpdateInfo(Map<String, Object> result) {
        LOG.info("addUpdateInfo()");
        autoShutOff.update();

        MBusDeviceBean[] devs = new MBusDeviceBean[master.deviceCount()];
        for (int i = 0; i < devs.length; i++) {
            MBusResponseFramesContainer dev = master.getDevice(i);
            devs[i] = new MBusDeviceBean(i, dev);
        }
        if(devs.length > 0){
        	message = new TranslatableMessage("dsEdit.mbus.tester.foundDevices" , devs.length).translate(Common.getTranslations());
        }
        result.put("addressing", mBusAddressing.getLabel());
        result.put("devices", devs);
        result.put("message", message);
        result.put("finished", finished);
    }

    @Override
    public void cancel() {
        LOG.info("cancel()");
        message = translations.translate("dsEdit.mbus.tester.cancelled");
        cleanup();
    }

    void cleanup() {
        LOG.info("cleanup()");
        if (!finished) {
            finished = true;
            try {
                master.cancel();
				master.close();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}

            autoShutOff.cancel();
            searchThread.interrupt();
        }
    }

    public void getDeviceDetails(int deviceIndex, Map<String, Object> result) {
        MBusResponseFramesContainer dev = master.getDevice(deviceIndex);
        result.put("addressing", mBusAddressing.getLabel());
        result.put("deviceName", String.format("%s %s 0x%02X %08d @0x%02X)", dev.getManufacturer(), dev.getMedium(),
                dev.getVersion(), dev.getIdentNumber(), dev.getAddress()));

        result.put("deviceIndex", deviceIndex);

        MBusResponseFrameBean[] responseFrames = new MBusResponseFrameBean[dev.getResponseFrameContainerCount()];
        for (int i = 0; i < dev.getResponseFrameContainerCount(); i++) {
            responseFrames[i] = new MBusResponseFrameBean(dev.getResponseFrameContainer(i).getResponseFrame(),
                    deviceIndex, i, dev.getResponseFrameContainer(i).getName());
        }
        result.put("responseFrames", responseFrames);
    }
    
    private MBusDiscovery(Translations translations, String host, int port,
            MBusAddressing mBusAddressing, int responseTimeoutOffset) {
        LOG.info("MBusDiscovery(...)");
        this.translations = translations;

        autoShutOff = new AutoShutOff(AutoShutOff.DEFAULT_TIMEOUT * 4) {
            @Override
            public void shutOff() {
                message = MBusDiscovery.this.translations.translate("dsEdit.mbus.tester.autoShutOff");
                MBusDiscovery.this.cleanup();
            }
        };

        this.mBusAddressing = mBusAddressing;
        // Thread starten , der sucht....
        master = new MBusMaster();
        try {
			Connection conn = new TcpIpConnection(host, port);
            master.setConnection(conn);
            master.open();
        }
        catch (Exception ex) {
            // no op
        	 LOG.warn("MBusDiscovery(...)", ex);
        } 
        // TODO master init
        message = translations.translate("dsEdit.mbus.tester.searchingDevices");
        searchThread = new SearchThread();
    }
    
}
