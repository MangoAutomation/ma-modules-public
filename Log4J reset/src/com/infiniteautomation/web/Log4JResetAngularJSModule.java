/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.web;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;

/**
 * @author Luis Güette
 */
public class Log4JResetAngularJSModule extends AngularJSModuleDefinition {
	@Override
	public String getJavaScriptFilename() {
		return "/angular/log4JReset.js";
	}
}
