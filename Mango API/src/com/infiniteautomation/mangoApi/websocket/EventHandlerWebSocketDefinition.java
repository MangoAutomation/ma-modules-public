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
public class EventHandlerWebSocketDefinition extends WebSocketDefinition{

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getWebSocketHandlerBeanName()
     */
    @Override
    public String getWebSocketHandlerBeanName() {
        return "eventHandlerWebSocketHandler";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
     */
    @Override
    public String getUrl() {
        return "/v1/websocket/event-handlers";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getTypeName()
     */
    @Override
    public String getTypeName() {
        return "EVENT_HANDLER";
    }
}
