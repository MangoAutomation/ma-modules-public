/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.envcan;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;

/**
 * @author Jared Wiltshire
 */
public class EnvCanDataSourceAngularJSModuleDefinition extends AngularJSModuleDefinition {
    @Override
    public String getJavaScriptFilename() {
        return "/angular/envcands.js";
    }
}
