/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * @author Terry Packer
 *
 */
public class MBusAddressSearchRequest extends MBusScanRequest {

    private byte firstAddress;
    private byte lastAddress;
    
    @Override
    public void validate(ProcessResult response) {
        // TODO Auto-generated method stub
        //TODO first must be before last
    }

    public byte getFirstAddress() {
        return firstAddress;
    }

    public void setFirstAddress(byte firstAddress) {
        this.firstAddress = firstAddress;
    }

    public byte getLastAddress() {
        return lastAddress;
    }

    public void setLastAddress(byte lastAddress) {
        this.lastAddress = lastAddress;
    }
    
}
