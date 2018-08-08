/*
 * Copyright (C) 2018 Infinite Automation Systems Inc. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1.reports;

import com.serotonin.m2m2.module.WebSocketDefinition;

/**
 * @author Jared Wiltshire
 */
public class ReportsWebSocketDefinition extends WebSocketDefinition{

    public static final String TYPE_NAME = "REPORTS";
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getWebSocketHandlerBeanName()
     */
    @Override
    public String getWebSocketHandlerBeanName() {
        return "reportWebSocketHandler";
    }
    @Override
    public String getUrl() {
        return "/v1/websocket/reports";
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

}
