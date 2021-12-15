/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/events")
public class EventsPerConnectionWebSocketHandler extends PerConnectionWebSocketHandler {

    /**
     */
    public EventsPerConnectionWebSocketHandler() {
        super(EventsWebSocketHandler.class);
    }

}
