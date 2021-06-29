/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.infiniteautomation.mango.maintenanceEvents.MaintenanceEventsJavascriptTestUtility;
import com.infiniteautomation.mango.maintenanceEvents.MaintenanceEventsJavascriptUtility;
import com.infiniteautomation.mango.util.script.ScriptUtility;
import com.serotonin.m2m2.module.MangoJavascriptContextObjectDefinition;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventsJavascriptUtilityDefinition extends MangoJavascriptContextObjectDefinition {

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MangoJavascriptContextObjectDefinition#getUtilityClass()
     */
    @Override
    protected Class<? extends ScriptUtility> getUtilityClass() {
        return MaintenanceEventsJavascriptUtility.class;
    }

    @Override
    protected Class<? extends ScriptUtility> getTestUtilityClass() {
        return MaintenanceEventsJavascriptTestUtility.class;
    }


}
