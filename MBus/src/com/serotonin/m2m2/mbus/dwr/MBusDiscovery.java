/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus.dwr;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Map;

import net.sf.mbus4j.MBusAddressing;
import net.sf.mbus4j.SerialPortTools;
import net.sf.mbus4j.dataframes.MBusResponseFramesContainer;
import net.sf.mbus4j.master.MBusMaster;
import net.sf.mbus4j.master.MasterEventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.web.dwr.beans.AutoShutOff;
import com.serotonin.m2m2.web.dwr.beans.TestingUtility;

/**
 * @author aploese
 */
public class MBusDiscovery implements MasterEventListener, TestingUtility {
    public static MBusDiscovery createPrimaryAddressingSearch(Translations translations, String commPortId,
            String phonenumber, int baudrate, int flowControlIn, int flowcontrolOut, int dataBits, int stopBits,
            int parity, int firstPrimaryAddress, int lastPrimaryAddress) {
        MBusDiscovery result = new MBusDiscovery(translations, commPortId, phonenumber, MBusAddressing.PRIMARY,
                baudrate, flowControlIn, flowcontrolOut, dataBits, stopBits, parity);
        result.firstPrimaryAddress = firstPrimaryAddress;
        result.lastPrimaryAddress = lastPrimaryAddress;
        result.searchThread.start();
        return result;
    }

    public static MBusDiscovery createSecondaryAddressingSearch(Translations translations, String commPortId,
            String phonenumber, int baudrate, int flowControlIn, int flowcontrolOut, int dataBits, int stopBits,
            int parity) {
        MBusDiscovery result = new MBusDiscovery(translations, commPortId, phonenumber, MBusAddressing.SECONDARY,
                baudrate, flowControlIn, flowcontrolOut, dataBits, stopBits, parity);
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
                    master.searchDevicesByPrimaryAddress(firstPrimaryAddress, lastPrimaryAddress);
                }
                else {
                    master.searchDevicesBySecondaryAddressing();
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
                sPort.close();
            }
            catch (InterruptedException ex) {
                LOG.info("Interrupted)");
            }
        }
    }

    static final Log LOG = LogFactory.getLog(MBusDiscovery.class);
    final Translations translations;
    // private final int removeDeviceIndex = 1;
    final MBusAddressing mBusAddressing;
    final MBusMaster master;
    SerialPort sPort;
    // private String phonenumber;
    private final AutoShutOff autoShutOff;
    String message;
    boolean finished;
    private final SearchThread searchThread;
    private String comPortId;
    private int baudrate;
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
            MBusAddressing mBusAddressing, int baudrate, int flowControlIn, int flowcontrolOut, int dataBits,
            int stopBits, int parity) {
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
            this.comPortId = comPortId;
            this.baudrate = baudrate;
            sPort = SerialPortTools.openPort(this.comPortId, this.baudrate);
            // sPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
            // sPort.setFlowControlMode(flowControlIn | flowcontrolOut);

            master.setStreams(sPort.getInputStream(), sPort.getOutputStream(), baudrate);
        }
        catch (NoSuchPortException ex) {
            LOG.warn("MBusDiscovery(...)", ex);
        }
        catch (PortInUseException ex) {
            LOG.warn("MBusDiscovery(...)", ex);
        }
        catch (UnsupportedCommOperationException ex) {
            LOG.warn("MBusDiscovery(...)", ex);
        }
        catch (IOException ex) {
            // no op
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
            master.cancel();
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
}
