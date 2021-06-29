/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.modules;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleNotificationListener.UpgradeState;

/**
 * 
 * @author Terry Packer
 */
@JsonInclude(Include.NON_NULL)
public class ModuleNotificationModel {

	private ModuleNotificationTypeEnum type;
	private String name;
	private String version;
	private UpgradeState upgradeProcessState;
	private String error;

	public ModuleNotificationModel(){ }
	
	/**
	 * @param type
	 * @param name
	 * @param version
	 */
	public ModuleNotificationModel(ModuleNotificationTypeEnum type, String name, String version, String error) {
		this.type = type;
		this.name = name;
		this.version = version;
		this.error = error;
	}

	/**
	 * 
	 * @param type
	 * @param name
	 * @param version
	 * @param upgradeProcessState
	 */
	public ModuleNotificationModel(ModuleNotificationTypeEnum type, UpgradeState upgradeProcessState) {
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
	public UpgradeState getUpgradeProcessState() {
		return upgradeProcessState;
	}
	public void setUpgradeProcessState(UpgradeState upgradeProcessState) {
		this.upgradeProcessState = upgradeProcessState;
	}
	
	@JsonGetter
	public TranslatableMessage getStateDescription() {
	    if(upgradeProcessState != null)
	        return upgradeProcessState.getDescription();
	    else
	        return null;
	}

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
	
}
