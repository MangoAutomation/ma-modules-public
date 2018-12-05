/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets.events;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;

/**
 * @author Jared Wiltshire
 */
@Component
@WebSocketMapping("/websocket/events")
public class EventsPerConnectionWebSocketHandler extends PerConnectionWebSocketHandler {

    public EventsPerConnectionWebSocketHandler() {
        super(EventsWebSocketHandler.class);
    }

}
