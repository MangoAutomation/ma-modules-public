/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.server;

/**
 * @author Terry Packer
 *
 */
public class NetworkInterfaceModel {

    private String interfaceName;
    private String hostAddress;
    public String getInterfaceName() {
        return interfaceName;
    }
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
    public String getHostAddress() {
        return hostAddress;
    }
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }
}
