/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets.pointValue;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;

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
