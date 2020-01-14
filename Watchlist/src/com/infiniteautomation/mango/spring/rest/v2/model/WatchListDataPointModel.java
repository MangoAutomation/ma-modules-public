/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.spring.rest.v2.model;

import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.vo.IDataPoint;

/**
 * @author Terry Packer
 *
 */
public class WatchListDataPointModel {

    private String xid;
    private String name;
    private String deviceName;
    private String readPermission;
    private String setPermission;

    public WatchListDataPointModel(){ }

    public WatchListDataPointModel(IDataPoint vo){
        this.xid = vo.getXid();
        this.name = vo.getName();
        this.deviceName = vo.getDeviceName();
        this.readPermission = PermissionService.implodeRoles(vo.getReadRoles());
        this.setPermission = PermissionService.implodeRoles(vo.getSetRoles());
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getReadPermission() {
        return readPermission;
    }
    public void setReadPermission(String readPermission) {
        this.readPermission = readPermission;
    }

    public String getSetPermission() {
        return setPermission;
    }
    public void setSetPermission(String setPermission) {
        this.setPermission = setPermission;
    }

    @Override
    public String toString() {
        return "XID: " + this.xid;
    }
}
