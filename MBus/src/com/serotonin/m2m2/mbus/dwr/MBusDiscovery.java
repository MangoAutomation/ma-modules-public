/*
 *   Mango - Open Source M2M - http://mango.serotoninsoftware.com
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.m2m2.mbus.dwr;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.mbus.MBusSearchByAddressing;
import com.serotonin.m2m2.mbus.MangoMBusSerialConnection;
import com.serotonin.m2m2.mbus.PrimaryAddressingSearch;
import com.serotonin.m2m2.mbus.SecondaryAddressingSearch;
import com.serotonin.m2m2.web.dwr.beans.AutoShutOff;
import com.serotonin.m2m2.web.dwr.beans.TestingUtility;

import net.sf.mbus4j.Connection;
import net.sf.mbus4j.SerialPortConnection;
import net.sf.mbus4j.dataframes.MBusResponseFramesContainer;
import net.sf.mbus4j.master.MBusMaster;
import net.sf.mbus4j.master.MasterEventListener;

/**
 * @author aploese
 */
public class MBusDiscovery implements MasterEventListener, TestingUtility {

    private final MBusSearchByAddressing searchByAddressing;

    public void start() {
        searchThread.start();
    }

    public MBusResponseFramesContainer getDevice(int deviceIndex) {
        return master.getDevice(deviceIndex);
    }

    public boolean changeAddress(int deviceIndex, byte newAddress, Map<String, Object> result) throws IOException, InterruptedException {
        master.open();
        try {
            MBusResponseFramesContainer mbrfc = getDevice(deviceIndex);
            if (master.setPrimaryAddressOfDevice((byte) newAddress, mbrfc)) {
                result.put("message", String.format("Changed primary Address of: %s %s 0x%02X %08d to 0x%02X", mbrfc.getManufacturer(), mbrfc.getMedium(), mbrfc.getVersion(), mbrfc.getIdentNumber(), mbrfc.getAddress()));
                return true;
            } else {
                result.put("message", String.format("Changing primary Address of: %s %s 0x%02X %08d to 0x%02X failed", mbrfc.getManufacturer(), mbrfc.getMedium(), mbrfc.getVersion(), mbrfc.getIdentNumber(), mbrfc.getAddress()));
                return false;
            }
        } finally {
            master.close();
        }
    }

    class SearchThread extends Thread {

        @Override
        public void run() {
            LOG.info("start search");
            try {
                master.open();
                if (searchByAddressing instanceof PrimaryAddressingSearch) {
                    final PrimaryAddressingSearch pas = (PrimaryAddressingSearch) searchByAddressing;
                    master.searchDevicesByPrimaryAddress(pas.firstAddr(), pas.lastAddr());
                } else if (searchByAddressing instanceof SecondaryAddressingSearch) {
                    final SecondaryAddressingSearch sasd = (SecondaryAddressingSearch) searchByAddressing;
                    master.widcardSearch(sasd.maskedId(), sasd.maskedManufacturer(), sasd.maskedVersion(), sasd.maskedMedium());
                } else {
                }
            } catch (InterruptedException ex) {
                LOG.info("Interrupted)");
            } catch (IOException ex) {
                LOG.warn("SearchThread.run", ex);
            } catch (Exception ex) {
                LOG.warn("SearchThread.run", ex);
            } finally {
                finished = true;
                LOG.info("Search finished!");
                try {
                    master.close();
                } catch (IOException ex) {
                    LOG.error("IO Ex)");
                }
            }
        }
    }
    static final Log LOG = LogFactory.getLog(MBusDiscovery.class);
    final Translations translations;
    final MBusMaster master;
    private final AutoShutOff autoShutOff;
    String message;
    boolean finished;
    private final SearchThread searchThread;
    private final Set<MBusDeviceBean> foundDevices;
    

    public MBusDiscovery(final Translations translations, Connection connection, MBusSearchByAddressing searchByAddressing) {
        LOG.info("MBusDiscovery(...)");
        this.translations = translations;
        this.foundDevices = new HashSet<MBusDeviceBean>();
        autoShutOff = new AutoShutOff(AutoShutOff.DEFAULT_TIMEOUT * 4) {

            @Override
            public void shutOff() {
                message = MBusDiscovery.this.translations.translate("dsEdit.mbus.tester.autoShutOff");
                MBusDiscovery.this.cleanup();
            }
        };

        // Thread starten , der sucht....
        master = new MBusMaster();
        if (connection instanceof SerialPortConnection) {
            //replace with buggy jssc
            SerialPortConnection spc = (SerialPortConnection) connection;
            String owner = "Mango MBus Serial Test Tool by " + Common.getUser().getUsername();
            master.setConnection(new MangoMBusSerialConnection(owner, spc.getPortName(), spc.getBitPerSecond(), 1000));
        } else {
            master.setConnection(connection);
        }
        this.searchByAddressing = searchByAddressing;
        message = this.translations.translate("dsEdit.mbus.tester.searchingDevices");
        searchThread = new SearchThread();
        
    }

    public void addUpdateInfo(Map<String, Object> result) {
        LOG.info("addUpdateInfo()");
        autoShutOff.update();

        int deviceCount = master.deviceCount();
        for (int i = 0; i < deviceCount; i++) {
            MBusResponseFramesContainer dev = master.getDevice(i);
            //Only keep newly found devices, unique set
            foundDevices.add(new MBusDeviceBean(i, dev));
        }
        
        result.put("devices", foundDevices);
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
        LOG.info("getDeviceDetails()");
        MBusResponseFramesContainer dev = master.getDevice(deviceIndex);
        result.put("deviceName", String.format("%s %s 0x%02X %08d @0x%02X", dev.getManufacturer(), dev.getMedium(),
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
