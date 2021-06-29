/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.rest.latest.model.permissions.MangoPermissionModel;
import com.serotonin.m2m2.vo.IDataPoint;

/**
 * @author Terry Packer
 *
 */
public class WatchListDataPointModel {

    private String xid;
    private String name;
    private String deviceName;
    private MangoPermissionModel readPermission;
    private MangoPermissionModel setPermission;

    public WatchListDataPointModel(){ }

    public WatchListDataPointModel(IDataPoint vo){
        this.xid = vo.getXid();
        this.name = vo.getName();
        this.deviceName = vo.getDeviceName();
        this.readPermission = new MangoPermissionModel(vo.getReadPermission());
        this.setPermission = new MangoPermissionModel(vo.getSetPermission());
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

    public MangoPermissionModel getReadPermission() {
        return readPermission;
    }
    public void setReadPermission(MangoPermissionModel readPermission) {
        this.readPermission = readPermission;
    }

    public MangoPermissionModel getSetPermission() {
        return setPermission;
    }
    public void setSetPermission(MangoPermissionModel setPermission) {
        this.setPermission = setPermission;
    }

    @Override
    public String toString() {
        return "XID: " + this.xid;
    }
}
