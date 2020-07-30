/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;

public class WebSocketResponse<T> implements WebSocketMessage {
    int sequenceNumber;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    T payload;

    public WebSocketResponse() {
    }

    public WebSocketResponse(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public WebSocketResponse(int sequenceNumber, T payload) {
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public WebSocketMessageType getMessageType() {
        return WebSocketMessageType.RESPONSE;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}