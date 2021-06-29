/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.realtime;

import java.util.Map;


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
    private Map<String,String> tags;
    private Map<String, Object> attributes;

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
    public Map<String, String> getTags() {
        return tags;
    }
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
