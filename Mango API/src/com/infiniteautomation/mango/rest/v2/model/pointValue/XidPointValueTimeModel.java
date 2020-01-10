/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

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
    private DataTypeEnum type;

    public DataTypeEnum getType() {
        return type;
    }
    public void setType(DataTypeEnum type) {
        this.type = type;
    }

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
