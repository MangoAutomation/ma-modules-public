/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.realtime;

import java.util.Map;

import com.serotonin.m2m2.rt.dataImage.RealTimeDataPointValue;

/**
 *
 * @author Terry Packer
 */
public class RealTimeDataPointValueModel {

    private String xid;
    private String deviceName;
    private String name;
    private Object value;
    private String renderedValue;
    private String type;
    private long timestamp;
    private String status;
    private String path;
    private Map<String,String> tags;
    
    public RealTimeDataPointValueModel(RealTimeDataPointValue data, Object value) {
        this.xid = data.getXid();
        this.deviceName = data.getDeviceName();
        this.name = data.getPointName();
        this.value = value;
        this.renderedValue = data.getRenderedValue();
        this.type = data.getPointType();
        this.timestamp = data.getTimestamp();
        this.status = data.getStatus();
        this.path = data.getPath();
        this.tags = data.getTags();
    }
    
    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }
    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public String getRenderedValue() {
        return renderedValue;
    }
    public void setRenderedValue(String renderedValue) {
        this.renderedValue = renderedValue;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public Map<String, String> getTags() {
        return tags;
    }
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
