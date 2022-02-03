/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue.streams;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * @author Jared Wiltshire
 */
@JsonInclude(Include.NON_NULL)
public class StreamPointValueTimeModel {
    Object value;
    Object timestamp;
    TranslatableMessage annotation;
    Boolean cached;
    Boolean bookend;
    String rendered;
    Object raw;
    String xid;
    String name;
    String deviceName;
    String dataSourceName;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public TranslatableMessage getAnnotation() {
        return annotation;
    }

    public void setAnnotation(TranslatableMessage annotation) {
        this.annotation = annotation;
    }

    public Boolean getCached() {
        return cached;
    }

    public void setCached(Boolean cached) {
        this.cached = cached;
    }

    public Boolean getBookend() {
        return bookend;
    }

    public void setBookend(Boolean bookend) {
        this.bookend = bookend;
    }

    public String getRendered() {
        return rendered;
    }

    public void setRendered(String rendered) {
        this.rendered = rendered;
    }

    public Object getRaw() {
        return raw;
    }

    public void setRaw(Object raw) {
        this.raw = raw;
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

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
}
