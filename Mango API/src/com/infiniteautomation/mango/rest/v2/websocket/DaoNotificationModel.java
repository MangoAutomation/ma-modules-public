/**
 * Copyright (C) 2015 Infinite Automation Systems. All rights reserved.
 * http://infiniteautomation.com/
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Jared Wiltshire
 */
public class DaoNotificationModel {
    /**
     * add, update or delete
     */
    @JsonProperty
    String action;

    /**
     * The vo object
     */
    @JsonProperty
    Object object;

    /**
     * Contains the xid of the object prior to an update (XIDs can be changed)
     */
    @JsonProperty
    String originalXid;

    public DaoNotificationModel() {
    }

    public DaoNotificationModel(String action, Object object) {
        this(action, object, null);
    }

    public DaoNotificationModel(String action, Object object, String originalXid) {
        this.action = action;
        this.object = object;
        this.originalXid = originalXid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getOriginalXid() {
        return originalXid;
    }

    public void setOriginalXid(String originalXid) {
        this.originalXid = originalXid;
    }
}
