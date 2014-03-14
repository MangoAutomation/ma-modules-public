/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class MappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/maintenance_events.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return null;
    }

    @Override
    public String getJspPath() {
        return "web/maintenanceEvents.jsp";
    }

    @Override
    public String getMenuKey() {
        return "header.maintenanceEvents";
    }

    @Override
    public String getMenuImage() {
        return "web/hammer.png";
    }

    @Override
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }
}
