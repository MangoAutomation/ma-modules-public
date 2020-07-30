/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import java.util.Date;

import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * @author Terry Packer
 *
 */
public class SerialTestResultModel {
    private boolean success;
    private String pointName;
    private String pointXid;
    private String identifier;
    private Object value;
    private Date timestamp;
    private TranslatableMessage error;
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getPointName() {
        return pointName;
    }
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }
    public String getPointXid() {
        return pointXid;
    }
    public void setPointXid(String pointXid) {
        this.pointXid = pointXid;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public TranslatableMessage getError() {
        return error;
    }
    public void setError(TranslatableMessage error) {
        this.error = error;
    }
}
