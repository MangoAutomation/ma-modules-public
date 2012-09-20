/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class ScheduledEventMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/scheduled_events.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return null;
    }

    @Override
    public String getJspPath() {
        return "web/scheduledEvents.jsp";
    }

    @Override
    public String getMenuKey() {
        return "header.scheduledEvents";
    }

    @Override
    public String getMenuImage() {
        return "web/clock.png";
    }

    @Override
    public Permission getPermission() {
        return Permission.DATA_SOURCE;
    }
}
