/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.pointValue;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;

/**
 * @author Jared Wiltshire
 */
@Component
@WebSocketMapping("/websocket/point-value")
public class PerConnectionPointValueWebSocketHandler extends PerConnectionWebSocketHandler {

    public PerConnectionPointValueWebSocketHandler() {
        super(PointValueWebSocketHandler.class);
    }
}
