/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.i18n.ProcessResult;

import net.sf.mbus4j.Connection;
import net.sf.mbus4j.TcpIpConnection;

/**
 * @author Terry Packer
 *
 */
public class MBusTcpIpAddressScanRequest extends MBusAddressScanRequest {

    private String host;
    private int port;
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void validate(ProcessResult response) {
        super.validate(response);
        if(StringUtils.isEmpty(host))
            response.addContextualMessage("host", "validate.required");
        if(port < 1)
            response.addContextualMessage("port", "validate.greaterThanZero");
        
    }

    @Override
    public Connection createConnection() {
        return new TcpIpConnection(host, port);
    }
}
