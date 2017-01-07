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
	
	private List<ModuleModel> upgrades;
	private List<ModuleModel> newInstalls;
	
	public ModuleUpgradesModel(){ }
	
	/**
	 * @param upgrades
	 * @param newInstalls
	 */
	public ModuleUpgradesModel(List<ModuleModel> upgrades, List<ModuleModel> newInstalls) {
		this.upgrades = upgrades;
		this.newInstalls = newInstalls;
	}

	public List<ModuleModel> getUpgrades() {
		return upgrades;
	}
	public void setUpgrades(List<ModuleModel> upgrades) {
		this.upgrades = upgrades;
	}
	public List<ModuleModel> getNewInstalls() {
		return newInstalls;
	}
	public void setNewInstalls(List<ModuleModel> newInstalls) {
		this.newInstalls = newInstalls;
	}

	/**
	 * @return
	 */
	public List<StringStringPair> fullModulesList() {
		List<StringStringPair> list = new ArrayList<StringStringPair>();
		if(upgrades != null){
			for(ModuleModel model : upgrades)
				list.add(new StringStringPair(model.name, model.version));
		}
		if(newInstalls != null){
			for(ModuleModel model : newInstalls)
				list.add(new StringStringPair(model.name, model.version));
		}
		return list;

	}

	
	
}
