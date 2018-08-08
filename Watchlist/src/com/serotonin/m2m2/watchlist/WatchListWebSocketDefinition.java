/*
   Copyright (C) 2016 Infinite Automation Systems Inc. All rights reserved.
   @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import com.serotonin.m2m2.module.WebSocketDefinition;

/**
 * @author Terry Packer
 *
 */
public class WatchListWebSocketDefinition extends WebSocketDefinition{

    public static final String TYPE_NAME = "WATCH_LIST";

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getWebSocketHandlerBeanName()
     */
    @Override
    public String getWebSocketHandlerBeanName() {
        return "watchListWebSocketHandler";
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
     */
    @Override
    public String getUrl() {
        return "/v1/websocket/watch-lists";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getTypeName()
     */
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

}
