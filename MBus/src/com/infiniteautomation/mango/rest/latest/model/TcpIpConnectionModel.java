/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import io.swagger.annotations.ApiModel;
import net.sf.mbus4j.TcpIpConnection;

/**
 * @author Terry Packer
 *
 */
@ApiModel(value="mbusTcpIp", parent=ConnectionModel.class)
@JsonTypeName("mbusTcpIp")
public class TcpIpConnectionModel extends ConnectionModel<TcpIpConnection> {

    public static final String TYPE_NAME = "mbusTcpIp";
    
    private String host;
    private int port;
    
    public TcpIpConnectionModel() { }
    public TcpIpConnectionModel(TcpIpConnection conn) {
        super(conn);
        this.host =  conn.getHost();
        this.port = conn.getPort();
    }
    
    @Override
    public TcpIpConnection toVO() {
        return new TcpIpConnection(host, port, bitPerSecond, responseTimeoutOffset);
    }
    
    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }
    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }
    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

}
