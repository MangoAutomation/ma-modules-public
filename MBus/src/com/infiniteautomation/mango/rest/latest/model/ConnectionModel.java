/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import net.sf.mbus4j.Connection;

/**
 * @author Terry Packer
 *
 */
@ApiModel(subTypes= {ConnectionModel.class, SerialPortConnectionModel.class, TcpIpConnectionModel.class}, discriminator="modelType")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property=ConnectionModel.MODEL_TYPE)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TcpIpConnectionModel.class, name="mbusTcpIp"),
    @JsonSubTypes.Type(value = SerialPortConnectionModel.class, name="mbusSerial")
})
public abstract class ConnectionModel <T extends Connection>{
    
    public static final String MODEL_TYPE = "modelType";
    
    protected int bitPerSecond;
    protected int responseTimeoutOffset;
    
    public ConnectionModel() { }
    public ConnectionModel(T conn) {
        this.bitPerSecond = conn.getBitPerSecond();
        this.responseTimeoutOffset = conn.getResponseTimeOutOffset();
    }
    
    public abstract T toVO();
    
    /**
     * @return the bitPerSecond
     */
    public int getBitPerSecond() {
        return bitPerSecond;
    }
    /**
     * @param bitPerSecond the bitPerSecond to set
     */
    public void setBitPerSecond(int bitPerSecond) {
        this.bitPerSecond = bitPerSecond;
    }
    /**
     * @return the responseTimeoutOffset
     */
    public int getResponseTimeoutOffset() {
        return responseTimeoutOffset;
    }
    /**
     * @param responseTimeoutOffset the responseTimeoutOffset to set
     */
    public void setResponseTimeoutOffset(int responseTimeoutOffset) {
        this.responseTimeoutOffset = responseTimeoutOffset;
    }
    
}
