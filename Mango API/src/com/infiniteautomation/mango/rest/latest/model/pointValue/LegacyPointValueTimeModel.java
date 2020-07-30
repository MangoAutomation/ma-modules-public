/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue;

/**
 * Use PointValueTimeModel instead, this is here for compatibility with
 * a few endpoints in the 2.0 api
 * @author Terry Packer
 */
@Deprecated
public class LegacyPointValueTimeModel {

    private DataTypeEnum dataType;
    private Object value;
    private long timestamp;
    private String annotation;

    public DataTypeEnum getDataType() {
        return dataType;
    }
    public void setDataType(DataTypeEnum type) {
        this.dataType = type;
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
