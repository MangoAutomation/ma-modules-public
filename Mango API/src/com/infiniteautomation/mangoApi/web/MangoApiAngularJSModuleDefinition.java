/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mangoApi.web;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;

/**
 * @author Jared Wiltshire
 */
public class MangoApiAngularJSModuleDefinition extends AngularJSModuleDefinition {
    @Override
    public String getJavaScriptFilename() {
        return "/angular/mangoApi.js";
    }
}
