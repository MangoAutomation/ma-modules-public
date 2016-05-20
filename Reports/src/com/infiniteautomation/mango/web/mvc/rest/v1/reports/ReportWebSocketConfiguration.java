/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.web.mvc.rest.v1.reports;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketConfigurer;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandshakeInterceptor;

/**
 * @author Terry Packer
 *
 */
@Configuration
@EnableWebSocket
public class ReportWebSocketConfiguration extends MangoWebSocketConfigurer {
    public static final ReportWebSocketHandler reportHandler = new ReportWebSocketHandler();
    
    /* (non-Javadoc)
     * @see org.springframework.web.socket.config.annotation.WebSocketConfigurer#registerWebSocketHandlers(org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry)
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(reportHandler, "/v1/websocket/reports")
            .setHandshakeHandler(handshakeHandler())
            .addInterceptors(new MangoWebSocketHandshakeInterceptor());
    }

}
