/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket;

public abstract class WebSocketRequest {
    int sequenceNumber;

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}