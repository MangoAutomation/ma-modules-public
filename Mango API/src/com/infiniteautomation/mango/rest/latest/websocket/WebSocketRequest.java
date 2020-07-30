/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

public abstract class WebSocketRequest implements WebSocketMessage {
    int sequenceNumber;

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public WebSocketMessageType getMessageType() {
        return WebSocketMessageType.REQUEST;
    }
}