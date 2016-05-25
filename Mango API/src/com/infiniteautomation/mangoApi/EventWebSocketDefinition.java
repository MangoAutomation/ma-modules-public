/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mangoApi;

import com.serotonin.m2m2.module.WebSocketDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.events.EventEventHandler;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
public class EventWebSocketDefinition extends WebSocketDefinition{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#getHandler()
	 */
	@Override
	public MangoWebSocketHandler getHandler() {
		return new EventEventHandler();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
	 */
	@Override
	public String getUrl() {
		return "/v1/websocket/events";
	}
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#perConnection()
	 */
	@Override
	public boolean perConnection() {
		return true;
	}
}
