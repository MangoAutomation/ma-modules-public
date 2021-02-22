/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.serotonin.m2m2.watchlist;

import com.infiniteautomation.mango.permission.MangoPermission;

public class WatchListPoint {
    private int id;
    private String xid;
    private String name;
    private String deviceName;
    private MangoPermission readPermission;
    private MangoPermission editPermission;
    private MangoPermission setPermission;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public MangoPermission getReadPermission() {
        return readPermission;
    }

    public void setReadPermission(MangoPermission readPermission) {
        this.readPermission = readPermission;
    }

    public MangoPermission getEditPermission() {
        return editPermission;
    }

    public void setEditPermission(MangoPermission editPermission) {
        this.editPermission = editPermission;
    }

    public MangoPermission getSetPermission() {
        return setPermission;
    }

    public void setSetPermission(MangoPermission setPermission) {
        this.setPermission = setPermission;
    }
}
