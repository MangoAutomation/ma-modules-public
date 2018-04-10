/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mangoApi.websocket;

import org.springframework.web.socket.WebSocketHandler;

import com.serotonin.m2m2.module.WebSocketDefinition;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.DataSourceWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
public class DataSourceWebSocketDefinition extends WebSocketDefinition{

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getHandlerSingleton()
     */
    @Override
    protected WebSocketHandler createHandler() {
        return new DataSourceWebSocketHandler();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
     */
    @Override
    public String getUrl() {
        return "/v1/websocket/data-sources";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getTypeName()
     */
    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.DATA_SOURCE;
    }
}
