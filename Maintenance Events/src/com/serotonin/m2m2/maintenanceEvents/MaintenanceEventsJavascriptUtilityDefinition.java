/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.infiniteautomation.mango.maintenanceEvents.MaintenanceEventsJavascriptUtility;
import com.infiniteautomation.mango.util.script.ScriptUtility;
import com.serotonin.m2m2.module.MangoJavascriptContextObjectDefinition;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventsJavascriptUtilityDefinition extends MangoJavascriptContextObjectDefinition {

    public static final String CONTEXT_KEY = "maintenanceEventsUtility";
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MangoJavascriptContextObjectDefinition#getContextKey()
     */
    @Override
    public String getContextKey() {
        return CONTEXT_KEY;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MangoJavascriptContextObjectDefinition#getUtilityClass()
     */
    @Override
    protected Class<? extends ScriptUtility> getUtilityClass() {
        return MaintenanceEventsJavascriptUtility.class;
    }


}
