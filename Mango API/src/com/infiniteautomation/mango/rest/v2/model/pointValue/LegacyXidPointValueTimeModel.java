/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.pointValue;

/**
 * Left here for legacy purposes in the point value rest controller.
 *
 * To import values consider using the PointValueModificationRestController
 * with the XidPointValueTimeModel.  The difference being
 * the time format.
 *
 * @author Terry Packer
 */
@Deprecated
public class LegacyXidPointValueTimeModel {

    private String xid;
    private Object value;
    private long timestamp;
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
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getAnnotation() {
        return annotation;
    }
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

}
