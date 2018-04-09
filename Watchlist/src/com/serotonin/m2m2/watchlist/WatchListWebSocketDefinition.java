/*
   Copyright (C) 2016 Infinite Automation Systems Inc. All rights reserved.
   @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import com.serotonin.m2m2.module.WebSocketDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.WatchListWebSocketHandler;
import com.serotonin.m2m2.web.mvc.websocket.MangoWebSocketPublisher;

/**
 * @author Terry Packer
 *
 */
public class WatchListWebSocketDefinition extends WebSocketDefinition{

	public static final String TYPE_NAME = "WATCH_LIST";
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.WebSocketDefinition#getHandler()
	 */
	@Override
	protected MangoWebSocketPublisher createHandler() {
		return new WatchListWebSocketHandler();
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
