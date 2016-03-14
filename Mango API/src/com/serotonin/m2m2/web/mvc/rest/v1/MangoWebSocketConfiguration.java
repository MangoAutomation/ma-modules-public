/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

import com.infiniteautomation.mangoApi.MangoApiModuleDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.events.EventEventHandler;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue.PointValueEventHandler;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketConfigurer;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandshakeInterceptor;

/**
 * 
 * TODO Make WebSocket Configurations for Modules and use the Core's configurer in 2.8.0 Release
 * @author Terry Packer
 *
 */
@Configuration
@EnableWebSocket
public class MangoWebSocketConfiguration extends MangoWebSocketConfigurer{
		
	//@See https://github.com/jetty-project/embedded-jetty-websocket-examples
	/* (non-Javadoc)
	 * @see org.springframework.web.socket.config.annotation.WebSocketConfigurer#registerWebSocketHandlers(org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry)
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

		//Setup Allowed Origins for CORS requests
		//TODO create a way to allow checking the origin headers property and refresh during runtime
		String originsString = MangoApiModuleDefinition.props.getString("Access-Control-Allow-Origin");
		String[] origins = null;
		boolean hasOrigins = false;
		if(!StringUtils.isEmpty(originsString)){
			hasOrigins = true;
			origins = originsString.split(",");
		}
		
		WebSocketHandlerRegistration registration = registry.addHandler(pointValueEventHandler(), "/v1/websocket/point-value")
		.setHandshakeHandler(handshakeHandler())
		.addInterceptors(new MangoWebSocketHandshakeInterceptor());
		if(hasOrigins)
			registration.setAllowedOrigins(origins);
		
		registration = registry.addHandler(mangoEventHandler(), "/v1/websocket/events")
		.setHandshakeHandler(handshakeHandler())
		.addInterceptors(new MangoWebSocketHandshakeInterceptor());		
		if(hasOrigins)
			registration.setAllowedOrigins(origins);
		
		registration = registry.addHandler(com.serotonin.m2m2.web.mvc.spring.MangoWebSocketConfiguration.jsonDataHandler, "/v1/websocket/json-data")
		.setHandshakeHandler(handshakeHandler())
		.addInterceptors(new MangoWebSocketHandshakeInterceptor());
		if(hasOrigins)
			registration.setAllowedOrigins(origins);
	}
	
	@Bean
	public WebSocketHandler pointValueEventHandler(){
		return new PerConnectionWebSocketHandler(PointValueEventHandler.class);
	}
	
	@Bean
	public WebSocketHandler mangoEventHandler(){
		return new PerConnectionWebSocketHandler(EventEventHandler.class);
	}
	
}
