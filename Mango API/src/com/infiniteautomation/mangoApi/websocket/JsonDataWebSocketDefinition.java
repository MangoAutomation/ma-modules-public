/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mangoApi.websocket;

import com.serotonin.m2m2.module.WebSocketDefinition;

/**
 * @author Terry Packer
 *
 */
public class JsonDataWebSocketDefinition extends WebSocketDefinition{

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getWebSocketHandlerBeanName()
     */
    @Override
    public String getWebSocketHandlerBeanName() {
        return "jsonDataWebSocketHandler";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
     */
    @Override
    public String getUrl() {
        return "/v1/websocket/json-data";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getTypeName()
     */
    @Override
    public String getTypeName() {
        return "JSON_DATA";
    }
}
