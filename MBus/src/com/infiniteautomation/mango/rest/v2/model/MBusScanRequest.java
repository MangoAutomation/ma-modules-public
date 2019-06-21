/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.Validatable;

import net.sf.mbus4j.Connection;

/**
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
@JsonSubTypes({
    @Type(value = MBusTcpIpAddressScanRequest.class, name="MBusTcpIpAddressScanRequest"),
    @Type(value = MBusTcpIpSecondaryAddressScanRequest.class, name="MBusTcpIpSecondaryAddressScanRequest"),
    @Type(value = MBusSerialAddressScanRequest.class, name="MBusSerialAddressScanRequest"),
    @Type(value = MBusSerialSecondaryAddressScanRequest.class, name="MBusSerialSecondaryAddressScanRequest")
})
public abstract class MBusScanRequest implements Validatable {

    protected int bitsPerSecond;
    protected int responseTimeoutOffset;
    private String dataSourceXid;

    public int getBitsPerSecond() {
        return bitsPerSecond;
    }

    public void setBitsPerSecond(int bitsPerSecond) {
        this.bitsPerSecond = bitsPerSecond;
    }

    public int getResponseTimeoutOffset() {
        return responseTimeoutOffset;
    }

    public void setResponseTimeoutOffset(int responseTimeoutOffset) {
        this.responseTimeoutOffset = responseTimeoutOffset;
    }

    public String getDataSourceXid() {
        return dataSourceXid;
    }
    
    public void setDataSourceXid(String dataSourceXid) {
        this.dataSourceXid = dataSourceXid;
    }
    
    @Override
    public void validate(ProcessResult response) {
        switch(bitsPerSecond) {
            case 300:
            case 2400:
            case 3600:
                break;
            default:
                response.addContextualMessage("bitsPerSecond", "validate.required");
        }
    }
    
    public abstract Connection createConnection();

}
