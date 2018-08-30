/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.rest.v2.util.CrudNotificationType;

public class WebSocketNotification<T> implements WebSocketMessage {
    /**
     * Use CrudNotificationType where possible
     */
    String notificationType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    T payload;

    public WebSocketNotification() {
    }

    public WebSocketNotification(String notificationType, T payload) {
        this.notificationType = notificationType;
        this.payload = payload;
    }

    public WebSocketNotification(CrudNotificationType notificationType, T payload) {
        this.notificationType = notificationType.getNotificationType();
        this.payload = payload;
    }

    @Override
    public WebSocketMessageType getMessageType() {
        return WebSocketMessageType.NOTIFICATION;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
}