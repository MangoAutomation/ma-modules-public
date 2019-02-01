/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import net.sf.mbus4j.SerialPortConnection;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value="mbusSerial", parent=ConnectionModel.class)
@JsonTypeName("mbusSerial")
public class SerialPortConnectionModel extends ConnectionModel<SerialPortConnection>{
    
    public static final String TYPE_NAME = "mbusSerial";
    
    private String commPortId;
    
    public SerialPortConnectionModel() { }
    public SerialPortConnectionModel(SerialPortConnection conn) {
        super(conn);
        this.commPortId = conn.getPortName();
    }

    @Override
    public SerialPortConnection toVO() {
        return new SerialPortConnection(commPortId, bitPerSecond, responseTimeoutOffset);
    }
    /**
     * @return the commPortId
     */
    public String getCommPortId() {
        return commPortId;
    }
    /**
     * @param commPortId the commPortId to set
     */
    public void setCommPortId(String commPortId) {
        this.commPortId = commPortId;
    }

}
