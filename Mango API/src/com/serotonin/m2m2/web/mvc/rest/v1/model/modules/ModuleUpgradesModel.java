/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.db.pair.StringStringPair;

/**
 * 
 * @author Terry Packer
 */
public class ModuleUpgradesModel {
	
	private List<ModuleUpgradeModel> upgrades;
	private List<ModuleUpgradeModel> newInstalls;
	
	public ModuleUpgradesModel(){ }
	
	/**
	 * @param upgrades
	 * @param newInstalls
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

	/**
	 * @return
	 */
	public List<StringStringPair> fullModulesList() {
		List<StringStringPair> list = new ArrayList<StringStringPair>();
		if(upgrades != null){
			for(ModuleUpgradeModel model : upgrades)
				list.add(new StringStringPair(model.getName(), model.getVersion()));
		}
		if(newInstalls != null){
			for(ModuleUpgradeModel model : newInstalls)
				list.add(new StringStringPair(model.getName(), model.getVersion()));
		}
		return list;

	}
}
