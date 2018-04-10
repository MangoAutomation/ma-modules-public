/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mangoApi.websocket;

import org.springframework.web.socket.WebSocketHandler;

import com.serotonin.m2m2.module.WebSocketDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.datapoint.DataPointWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
public class DataPointWebSocketDefinition extends WebSocketDefinition{

    public static final String TYPE_NAME = "DATA_POINT";

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getHandlerSingleton()
     */
    @Override
    public WebSocketHandler createHandler() {
        return new DataPointWebSocketHandler();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
     */
    @Override
    public String getUrl() {
        return "/v1/websocket/data-points";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getTypeName()
     */
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }
}
