/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.module.WebSocketDefinition;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventWebSocketDefinition extends WebSocketDefinition{

    public static final String TYPE_NAME = "MAINTENANCE_EVENTS";

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getWebSocketHandlerBeanName()
     */
    @Override
    public String getWebSocketHandlerBeanName() {
        return "maintenanceEventWebSocketHandler";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
     */
    @Override
    public String getUrl() {
        return "/v2/websocket/maintenance-events";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getTypeName()
     */
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

}
