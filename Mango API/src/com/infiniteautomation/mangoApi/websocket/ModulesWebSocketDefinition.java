/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mangoApi.websocket;

import com.serotonin.m2m2.module.WebSocketDefinition;

/**
 *
 * @author Terry Packer
 */
public class ModulesWebSocketDefinition extends WebSocketDefinition{

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getWebSocketHandlerBeanName()
     */
    @Override
    public String getWebSocketHandlerBeanName() {
        return "modulesWebSocketHandler";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getUrl()
     */
    @Override
    public String getUrl() {
        return "/v1/websocket/modules";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.WebSocketDefinition#getTypeName()
     */
    @Override
    public String getTypeName() {
        return "MODULES";
    }

}
