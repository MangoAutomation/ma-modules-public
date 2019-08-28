/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket;

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
     * @param handlerType
     */
    public EventsPerConnectionWebSocketHandler() {
        super(EventsWebSocketHandler.class);
    }

}
