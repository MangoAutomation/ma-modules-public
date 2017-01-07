/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

/**
 * 
 * @author Terry Packer
 */
public class ModuleModel {
	String name;
	String version;
	
	public ModuleModel(){ }
	
	/**
	 * @param name
	 * @param version
	 */
	public ModuleModel(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	
}
