/*
 * Copyright (C) 2018 Infinite Automation Systems Inc. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v1.reports;

import org.springframework.web.socket.WebSocketHandler;

import com.serotonin.m2m2.module.WebSocketDefinition;

/**
 * @author Jared Wiltshire
 */
public class ReportsWebSocketDefinition extends WebSocketDefinition{

    public static final String TYPE_NAME = "REPORTS";

    @Override
    protected WebSocketHandler createHandler() {
        return new ReportWebSocketHandler();
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
