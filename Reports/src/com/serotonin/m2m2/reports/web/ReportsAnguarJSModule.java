/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.reports.web;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;

/**
 * @author Jared Wiltshire
 */
public class ReportsAnguarJSModule extends AngularJSModuleDefinition {
	@Override
	public String getJavaScriptFilename() {
		return "/reports.js";
	}
}
