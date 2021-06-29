/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import java.time.ZonedDateTime;

/**
 * @author Terry Packer
 *
 */
public class XidPointValueTimeModel {
    private String xid;
    private Object value;
    private ZonedDateTime timestamp;
    private String annotation;

    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public String getAnnotation() {
        return annotation;
    }
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
    @Override
    public String toString() {
        return xid + " - " + value + "@" + timestamp;
    }
}
