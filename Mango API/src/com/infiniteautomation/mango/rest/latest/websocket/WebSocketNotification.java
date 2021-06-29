/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.rest.latest.util.CrudNotificationType;

public final class WebSocketNotification<T> implements WebSocketMessage {
    /**
     * Use CrudNotificationType (create/update/delete) where possible
     */
    String notificationType;

    /**
     * Typically a VO model
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    T payload;

    /**
     * For DAO notifications will contain originalXid and initiatorId
     */
    Map<String, Object> attributes;

    public WebSocketNotification() {
    }

    public WebSocketNotification(CrudNotificationType notificationType, T payload) {
        this(notificationType.getNotificationType(), payload, Collections.emptyMap());
    }

    public WebSocketNotification(String notificationType, T payload) {
        this(notificationType, payload, Collections.emptyMap());
    }

    public WebSocketNotification(CrudNotificationType notificationType, T payload, Map<String, Object> attributes) {
        this(notificationType.getNotificationType(), payload, attributes);
    }

    public WebSocketNotification(String notificationType, T payload, Map<String, Object> attributes) {
        this.notificationType = notificationType;
        this.payload = payload;
        this.attributes = Objects.requireNonNull(attributes);
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

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = Objects.requireNonNull(attributes);
    }
}