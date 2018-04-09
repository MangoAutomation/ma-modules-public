/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher;

import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceWebSocketHandler;
import com.serotonin.m2m2.module.WebSocketDefinition;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketPublisher;

public class TemporaryResourceWebSocketDefinition extends WebSocketDefinition {
    
    public static final String TYPE_NAME = "TEMPORARY_RESOURCE_WEBSOCKET";

    @Override
    protected MangoWebSocketPublisher createHandler() {
        return new TemporaryResourceWebSocketHandler();
    }

    @Override
    public String getUrl() {
        return "/v2/websocket/temporary-resources";
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }
}