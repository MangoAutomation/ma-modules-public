/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import com.infiniteautomation.mango.rest.v2.websocket.WebSocketNotification;

/**
 * @author Jared Wiltshire
 */
public class DaoNotificationModelV2 extends WebSocketNotification<Object> {
    String initiatorId;
    String originalXid;

    /**
     * @param type
     * @param voModel
     * @param originalXid
     * @param initiatorId
     */
    public DaoNotificationModelV2(String type, Object voModel, String originalXid, String initiatorId) {
        super(type, voModel);
        this.originalXid = originalXid;
        this.initiatorId = initiatorId;
    }

    public String getInitiatorId() {
        return initiatorId;
    }
    public void setInitiatorId(String initiatorId) {
        this.initiatorId = initiatorId;
    }
    public String getOriginalXid() {
        return originalXid;
    }
    public void setOriginalXid(String originalXid) {
        this.originalXid = originalXid;
    }
}
