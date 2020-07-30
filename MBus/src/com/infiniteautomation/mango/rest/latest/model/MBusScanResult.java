/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import java.util.List;

/**
 * @author Terry Packer
 *
 */
public class MBusScanResult {
    
    //Result data
    private List<MBusDeviceScanResult> devices;
    
    public MBusScanResult() {
        
    }
    
    public List<MBusDeviceScanResult> getDevices() {
        return devices;
    }
    
    public void setDevices(List<MBusDeviceScanResult> devices) {
        this.devices = devices;
    }
    

    
}
