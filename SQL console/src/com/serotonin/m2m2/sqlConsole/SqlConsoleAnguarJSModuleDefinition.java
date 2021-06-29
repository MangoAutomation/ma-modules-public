/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.sqlConsole;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;

/**
 * @author Jared Wiltshire
 */
public class SqlConsoleAnguarJSModuleDefinition extends AngularJSModuleDefinition {
    @Override
    public String getJavaScriptFilename() {
        return "/angular/sqlConsole.js";
    }
}
