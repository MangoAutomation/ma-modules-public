/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mangoApi.websocket;

import com.serotonin.m2m2.module.WebSocketDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.publisher.ModulesWebSocketHandler;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketHandler;

/**
 * 
 * @author Terry Packer
 */
public class ModulesWebSocketDefinition extends WebSocketDefinition{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#getHandler()
	 */
	@Override
	protected MangoWebSocketHandler getHandler() {
		return new ModulesWebSocketHandler();
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
	 */
	@Override
	public String getUrl() {
		return "/v1/websocket/modules";
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#perConnection()
	 */
	@Override
	public boolean perConnection() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return "MODULES";
	}

}
