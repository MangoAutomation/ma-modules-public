/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

/**
 * 
 * @author Terry Packer
 */
public class ModuleNotificationModel {

	private ModuleNotificationTypeEnum type;
	private String name;
	private String version;
	private String upgradeProcessState;

	public ModuleNotificationModel(){ }
	
	/**
	 * @param type
	 * @param name
	 * @param version
	 */
	public ModuleNotificationModel(ModuleNotificationTypeEnum type, String name, String version) {
		this.type = type;
		this.name = name;
		this.version = version;
	}

	/**
	 * 
	 * @param type
	 * @param name
	 * @param version
	 * @param upgradeProcessState
	 */
	public ModuleNotificationModel(ModuleNotificationTypeEnum type, String upgradeProcessState) {
		this.type = type;
		this.upgradeProcessState = upgradeProcessState;
	}

	public ModuleNotificationTypeEnum getType() {
		return type;
	}
	public void setType(ModuleNotificationTypeEnum type) {
		this.type = type;
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
	public String getUpgradeProcessState() {
		return upgradeProcessState;
	}
	public void setUpgradeProcessState(String upgradeProcessState) {
		this.upgradeProcessState = upgradeProcessState;
	}
}
