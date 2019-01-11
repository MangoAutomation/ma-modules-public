package com.infiniteautomation.mangoApi.web;
import com.serotonin.m2m2.module.AngularJSModuleDefinition;
 
public class MangoApiAngularJSModuleDefinition extends AngularJSModuleDefinition {
	@Override
	public String getJavaScriptFilename() {
		return "/angular/mangoApi.js";
	}
}
