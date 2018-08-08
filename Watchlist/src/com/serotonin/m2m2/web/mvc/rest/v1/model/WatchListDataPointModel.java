/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infiniteautomation.mango.spring.dao.DataPointDao;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Terry Packer
 *
 */
public class WatchListDataPointModel{

    private String xid;
    private String name;
    private String deviceName;
    private int pointFolderId;
    private String readPermission;
    private String setPermission;
	
	public WatchListDataPointModel(){ }
	
	public WatchListDataPointModel(DataPointVO vo){
		this.xid = vo.getXid();
		this.name = vo.getName();
		this.deviceName = vo.getDeviceName();
		this.pointFolderId = vo.getPointFolderId();
		this.readPermission = vo.getReadPermission();
		this.setPermission = vo.getSetPermission();
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

    public int getPointFolderId() {
        return pointFolderId;
    }
    public void setPointFolderId(int pointFolderId) {
        this.pointFolderId = pointFolderId;
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

    @JsonIgnore
	public DataPointVO getDataPointVO() {
		return DataPointDao.instance.getByXid(xid);
	}
	
}
