/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventUriMappingDefinition extends UriMappingDefinition{

        @Override
        public Permission getPermission() {
            return Permission.ADMINISTRATOR;
        }

        @Override
        public String getPath() {
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
}
