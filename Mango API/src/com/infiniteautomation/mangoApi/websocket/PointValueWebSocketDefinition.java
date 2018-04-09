/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mangoApi.websocket;

import org.springframework.web.socket.WebSocketHandler;

import com.serotonin.m2m2.module.PerConnectionWebSocketDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue.PointValueWebSocketHandler;

/**
 * @author Terry Packer
 */
public class PointValueWebSocketDefinition extends PerConnectionWebSocketDefinition {

    @Override
    public Class<? extends WebSocketHandler> getHandlerClass() {
        return PointValueWebSocketHandler.class;
    }
    
	@Override
	public String getUrl() {
		return "/v1/websocket/point-value";
	}

	@Override
	public String getTypeName() {
		return "POINT_VALUE";
	}
}
