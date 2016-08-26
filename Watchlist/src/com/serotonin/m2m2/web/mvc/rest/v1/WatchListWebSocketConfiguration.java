/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketConfigurer;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandshakeInterceptor;

/**
 * @author Terry Packer
 *
 */
public class WatchListWebSocketConfiguration extends MangoWebSocketConfigurer {
    public static final WatchListWebSocketHandler handler = new WatchListWebSocketHandler();
    
    /* (non-Javadoc)
     * @see org.springframework.web.socket.config.annotation.WebSocketConfigurer#registerWebSocketHandlers(org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry)
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/v1/websocket/watch-lists")
            .setHandshakeHandler(handshakeHandler())
            .addInterceptors(new MangoWebSocketHandshakeInterceptor());
    }
}