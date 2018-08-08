/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.MangoJavascriptContextObjectDefinition;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventJavascriptUtilityDefinition extends MangoJavascriptContextObjectDefinition {

    public static final String CONTEXT_KEY = "maintenanceEventsUtility";
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MangoJavascriptContextObjectDefinition#getContextKey()
     */
    @Override
    public String getContextKey() {
        return CONTEXT_KEY;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MangoJavascriptContextObjectDefinition#getContextObject()
     */
    @Override
    public Object getContextObject(PermissionHolder holder) {
        //TODO wire in holder and deal with singleton bean (Maybe a factory pattern?)
        return Common.getRuntimeContext().getBean("maintenanceEventsJavascriptUtility");
    }

}
