/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mangoApi.websocket;

import com.serotonin.m2m2.module.WebSocketDefinition;

public class TemporaryResourceWebSocketDefinition extends WebSocketDefinition {

    public static final String TYPE_NAME = "TEMPORARY_RESOURCE_WEBSOCKET";

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getWebSocketHandlerBeanName()
     */
    @Override
    public String getWebSocketHandlerBeanName() {
        return "temporaryResourceWebSocketHandler";
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