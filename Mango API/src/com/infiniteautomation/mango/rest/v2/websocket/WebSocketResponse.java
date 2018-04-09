/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket;

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