/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.modules;

import java.util.ArrayList;
import java.util.List;

import com.github.zafarkhaja.semver.Version;
import com.serotonin.db.pair.StringStringPair;

/**
 * 
 * @author Terry Packer
 */
public class ModuleUpgradesModel {
	
	private List<ModuleUpgradeModel> upgrades;
	private List<ModuleUpgradeModel> newInstalls;
	private List<ModuleModel> unavailableModules;

	public ModuleUpgradesModel(){ }
	
	/**
     */
	public ModuleUpgradesModel(List<ModuleUpgradeModel> upgrades, List<ModuleUpgradeModel> newInstalls) {
		this.upgrades = upgrades;
		this.newInstalls = newInstalls;
	}

	public List<ModuleUpgradeModel> getUpgrades() {
		return upgrades;
	}
	public void setUpgrades(List<ModuleUpgradeModel> upgrades) {
		this.upgrades = upgrades;
	}
	public List<ModuleUpgradeModel> getNewInstalls() {
		return newInstalls;
	}
	public void setNewInstalls(List<ModuleUpgradeModel> newInstalls) {
		this.newInstalls = newInstalls;
	}

	public List<ModuleModel> getUnavailableModules() {
		return unavailableModules;
	}

	public void setUnavailableModules(List<ModuleModel> unavailableModules) {
		this.unavailableModules = unavailableModules;
	}

	/**
     */
	public List<StringStringPair> fullModulesList() {
		List<StringStringPair> list = new ArrayList<StringStringPair>();
		if(upgrades != null){
			for(ModuleUpgradeModel model : upgrades)
				list.add(new StringStringPair(model.getName(), Version.valueOf(model.getNewVersion()).getNormalVersion()));
		}
		if(newInstalls != null){
			for(ModuleUpgradeModel model : newInstalls)
				list.add(new StringStringPair(model.getName(), Version.valueOf(model.getNewVersion()).getNormalVersion()));
		}
		return list;

	}
}
