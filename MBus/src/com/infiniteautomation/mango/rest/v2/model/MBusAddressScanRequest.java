/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.serotonin.m2m2.i18n.ProcessResult;

/**
 * @author Terry Packer
 *
 */
public abstract class MBusAddressScanRequest extends MBusScanRequest {

    private byte firstAddress;
    private byte lastAddress;
    
    @Override
    public void validate(ProcessResult response) {
        super.validate(response);
        if((firstAddress & 0xFF) < 0)
            response.addContextualMessage("firstAddress", "validate.greaterThanZero");
        if((lastAddress & 0xFF) < 0)
            response.addContextualMessage("firstAddress", "validate.greaterThanZero");
        if((firstAddress & 0xFF) > (lastAddress & 0xFF))
            response.addContextualMessage("lastAddress", "validate.greaterThanOrEqualTo", firstAddress);
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
