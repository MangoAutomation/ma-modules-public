/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;


/**
 *
 * Web Socket Configuration
 *
 * CORS allowed origins in env.properties are set here
 * WebSocketHandlers module definitions are registered here
 *
 * @author Terry Packer
 *
 */
@Configuration("mangoWebSocketV2Configuration")
@EnableWebSocket
@ComponentScan(basePackages = {"com.infiniteautomation.mango.rest.v2"})
public class MangoWebSocketConfiguration implements WebSocketConfigurer {

    @Autowired
    @Qualifier("mangoWebSocketHandshakeInterceptorV2")
    private HandshakeInterceptor handshakeInterceptor;

    @Autowired
    @Qualifier("mangoWebSocketHandshakeHandlerV2")
    private HandshakeHandler handshakeHandler;

    @Value("${rest.cors.enabled:false}")
    private boolean corsEnabled;

    @Value("${rest.cors.allowedOrigins}")
    private String[] allowedOrigins;

    @Autowired(required = false)
    private Set<WebSocketHandler> handlers = Collections.emptySet();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        for (WebSocketHandler handler : handlers) {
            WebSocketMapping mapping = handler.getClass().getAnnotation(WebSocketMapping.class);
            if (mapping != null) {
                for (String url : mapping.value()) {
                    WebSocketHandlerRegistration registration = registry.addHandler(handler, url)
                            .setHandshakeHandler(handshakeHandler)
                            .addInterceptors(handshakeInterceptor);

                    // Use allowed origins from CORS configuration
                    if (corsEnabled) {
                        registration.setAllowedOrigins(allowedOrigins);
                    }
                }
            }
        }
    }
}
