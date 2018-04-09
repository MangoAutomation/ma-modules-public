/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mangoApi.websocket;

import org.springframework.web.socket.WebSocketHandler;

import com.serotonin.m2m2.module.PerConnectionWebSocketDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.events.EventsWebSocketHandler;

/**
 * @author Terry Packer
 */
public class EventWebSocketDefinition extends PerConnectionWebSocketDefinition {

    @Override
    public Class<? extends WebSocketHandler> getHandlerClass() {
        return EventsWebSocketHandler.class;
    }

    @Override
    public String getUrl() {
        return "/v1/websocket/events";
    }

    @Override
    public String getTypeName() {
        return "EVENT";
    }
    
}
