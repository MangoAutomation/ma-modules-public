/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.maintenanceEvents;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;

/**
 * @author Luis Güette
 */
public class MaintenanceEventsAngularJSModule extends AngularJSModuleDefinition {
    @Override
    public String getJavaScriptFilename() {
        return "/angular/maintenanceEvents.js";
    }
}