/**
 * Copyright (C) 2015 Infinite Automation Systems. All rights reserved.
 * http://infiniteautomation.com/
 */
package com.infiniteautomation.mango.rest.latest.websocket;

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
     * The id of the object
     */
    @JsonProperty
    Integer id;

    /**
     * The current xid of the object
     */
    @JsonProperty
    String xid;

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

    /**
     * Contains the the object prior to an update
     */
    @JsonProperty
    Object originalObject;

    public DaoNotificationModel() {
    }

    public DaoNotificationModel(String action, Integer id, String xid, Object object, String originalXid, Object originalObject) {
        this.action = action;
        this.id = id;
        this.xid = xid;
        this.object = object;
        this.originalObject = originalObject;
        this.originalXid = originalXid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
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

    public Object getOriginalObject() {
        return originalObject;
    }

    public void setOriginalObject(Object originalObject) {
        this.originalObject = originalObject;
    }

}
