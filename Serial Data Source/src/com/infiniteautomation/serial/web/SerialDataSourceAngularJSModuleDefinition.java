/**
 * Copyright (C) 2019 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.serial.web;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;

/**
 * @author Luis Güette
 */
public class SerialDataSourceAngularJSModuleDefinition extends AngularJSModuleDefinition {
    @Override
    public String getJavaScriptFilename() {
        return "/angular/serial.js";
    }
}