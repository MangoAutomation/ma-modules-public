/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus.dwr;

import net.sf.mbus4j.dataframes.MBusResponseFramesContainer;

/**
 * 
 * @author aploese
 */
public class MBusDeviceBean {
    private final int index;
    private final MBusResponseFramesContainer dev;

    public static boolean compare(MBusResponseFramesContainer dev, String address, String id, String man,
            String medium, String version) {
        boolean result = address.equals(String.format("0x%02X", dev.getAddress()));
        result &= id.equals(String.format("%08d", dev.getIdentNumber()));
        result &= man.equals(dev.getManufacturer());
        result &= medium.equals(dev.getMedium().name());
        result &= version.equals(String.format("0x%02X", dev.getAddress()));
        return result;
    }

    public MBusDeviceBean(int index, MBusResponseFramesContainer dev) {
        this.index = index;
        this.dev = dev;
    }

    /**
     * @return the address
     */
    public byte getAddress() {
        return dev.getAddress();
    }

    /**
     * @return the address
     */
    public String getAddressHex() {
        return String.format("0x%02X", dev.getAddress());
    }

    /**
     * @return the id
     */
    public String getIdentNumber() {
        return String.format("%08d", dev.getIdentNumber());
    }

    /**
     * @return the man
     */
    public String getManufacturer() {
        return dev.getManufacturer();
    }

    /**
     * @return the medium
     */
    public String getMedium() {
        return dev.getMedium().name();
    }

    /**
     * @return the version
     */
    public byte getVersion() {
        return dev.getVersion();
    }

    /**
     * @return the version
     */
    public String getVersionHex() {
        return String.format("0x%02X", dev.getVersion());
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }
}
