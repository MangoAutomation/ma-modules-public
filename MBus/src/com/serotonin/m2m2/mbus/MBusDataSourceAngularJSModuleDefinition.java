/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;

/**
 * @author Jared Wiltshire
 */
public class MBusDataSourceAngularJSModuleDefinition extends AngularJSModuleDefinition {
    @Override
    public String getJavaScriptFilename() {
        return "/angular/mbus.js";
    }
}
