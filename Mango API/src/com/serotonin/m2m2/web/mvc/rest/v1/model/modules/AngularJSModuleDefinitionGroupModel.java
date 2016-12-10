/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

import java.util.List;

/**
 * Container for a group of AngularJS Module Definitions
 * 
 * @author Terry Packer
 */
public class AngularJSModuleDefinitionGroupModel {
	
	private List<String> urls;

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

}
