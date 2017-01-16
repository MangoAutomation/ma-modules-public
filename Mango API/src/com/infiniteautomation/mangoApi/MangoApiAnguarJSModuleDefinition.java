/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mangoApi;

import com.serotonin.m2m2.module.AngularJSModuleDefinition;

/**
 * @author Jared Wiltshire
 */
public class MangoApiAnguarJSModuleDefinition extends AngularJSModuleDefinition {
	@Override
	public String getJavaScriptFilename() {
		return "/mangoApi.js";
	}
}
