/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.internal;
import com.serotonin.m2m2.module.AngularJSModuleDefinition;
 
public class InternalDataSourceAngularJSModuleDefinition extends AngularJSModuleDefinition {
	@Override
	public String getJavaScriptFilename() {
		return "/angular/internal.js";
	}
}