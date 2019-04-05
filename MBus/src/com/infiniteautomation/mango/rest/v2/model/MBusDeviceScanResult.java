/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.mbus4j.dataframes.ResponseFrameContainer;
import net.sf.mbus4j.devices.GenericDevice;

/**
 * @author Terry Packer
 *
 */
public class MBusDeviceScanResult {

    private byte address;
    private int identNumber;
    private String manufacturer;
    private String medium;
    private byte version;
    //Response Frames info
    private List<MBusResponseFrameModel> responseFrames;

    public MBusDeviceScanResult(GenericDevice c) {
        this.address = c.getAddress();
        this.identNumber = c.getIdentNumber();
        this.manufacturer = c.getManufacturer();
        this.medium = c.getMedium().label;
        this.version = c.getVersion();
        this.responseFrames = new ArrayList<>();
        for(ResponseFrameContainer container : c.getResponseFrameContainers()) {
            this.responseFrames.add(new MBusResponseFrameModel(container.getName(), container.getResponseFrame()));
        }
    }

    public byte getAddress() {
        return address;
    }

    public void setAddress(byte address) {
        this.address = address;
    }

    public int getIdentNumber() {
        return identNumber;
    }

    public void setIdentNumber(int identNumber) {
        this.identNumber = identNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public List<MBusResponseFrameModel> getResponseFrames() {
        return responseFrames;
    }

    public void setResponseFrames(List<MBusResponseFrameModel> responseFrames) {
        this.responseFrames = responseFrames;
    }
}
