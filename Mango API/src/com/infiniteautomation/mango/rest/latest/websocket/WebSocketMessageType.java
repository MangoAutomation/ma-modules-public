/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import com.fasterxml.jackson.databind.JsonNode;

public enum WebSocketMessageType {
    REQUEST, RESPONSE, NOTIFICATION;

    public boolean messageTypeMatches(JsonNode tree) {
        if (!tree.isObject()) return false;

        JsonNode messageType = tree.get("messageType");
        if (messageType == null || !this.name().equals(messageType.textValue())) {
            return false;
        }

        return true;
    }
}