/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.i18n.ProcessResult;

import net.sf.mbus4j.Connection;
import net.sf.mbus4j.SerialPortConnection;

/**
 * @author Terry Packer
 *
 */
public class MBusSerialSecondaryAddressScanRequest extends MBusSecondaryAddressScanRequest{

    private String commPortId;
    
    @Override
    public void validate(ProcessResult response) {
        super.validate(response);
        if(StringUtils.isEmpty(commPortId))
            response.addContextualMessage("commPortId", "validate.invalidValue");
    }
    
    public String getCommPortId() {
        return commPortId;
    }

    public void setCommPortId(String commPortId) {
        this.commPortId = commPortId;
    }

    @Override
    public Connection createConnection() {
        return new SerialPortConnection(commPortId, bitsPerSecond, responseTimeoutOffset);
    }
}
