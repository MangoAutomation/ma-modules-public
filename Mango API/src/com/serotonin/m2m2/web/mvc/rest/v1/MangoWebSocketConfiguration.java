/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.serotonin.m2m2.web.mvc.rest.v1.publisher.events.EventEventHandler;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue.PointValueEventHandler;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandshakeInterceptor;

/**
 * @author Terry Packer
 *
 */
@Configuration
@EnableWebSocket
public class MangoWebSocketConfiguration implements WebSocketConfigurer{
	
	//@See https://github.com/jetty-project/embedded-jetty-websocket-examples
	/* (non-Javadoc)
	 * @see org.springframework.web.socket.config.annotation.WebSocketConfigurer#registerWebSocketHandlers(org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry)
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

		registry.addHandler(pointValueEventHandler(), "/v1/websocket/point-value")
		.setHandshakeHandler(handshakeHandler())
		.addInterceptors(new MangoWebSocketHandshakeInterceptor());
		
		registry.addHandler(mangoEventHandler(), "/v1/websocket/events")
		.setHandshakeHandler(handshakeHandler())
		.addInterceptors(new MangoWebSocketHandshakeInterceptor());		
	}
	
	@Bean
	public WebSocketHandler pointValueEventHandler(){
		return new PerConnectionWebSocketHandler(PointValueEventHandler.class);
	}
	
	@Bean
	public WebSocketHandler mangoEventHandler(){
		return new PerConnectionWebSocketHandler(EventEventHandler.class);
	}

	@Bean
    public DefaultHandshakeHandler handshakeHandler() {

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        policy.setInputBufferSize(8192);
        policy.setIdleTimeout(Integer.MAX_VALUE); //We don't want timeouts..
        //policy.setAsyncWriteTimeout(2000); //Default 60s
        WebSocketServerFactory factory = new WebSocketServerFactory(policy);
        
        return new DefaultHandshakeHandler(
                new JettyRequestUpgradeStrategy(factory));
    }
	
}
