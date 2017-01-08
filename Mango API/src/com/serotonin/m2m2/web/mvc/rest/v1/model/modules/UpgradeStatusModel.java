/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.modules;

import java.util.List;

/**
 * 
 * @author Terry Packer
 */
public class UpgradeStatusModel {
	
	boolean running;
	boolean finished;
	boolean cancelled;
	boolean willRestart;
	String stage;
	List<ModuleModel> results;
	String error;
	
	public boolean isRunning(){
		return running;
	}
	public void setRunning(boolean running){
		this.running = running;
	}
	
	public boolean isFinished() {
		return finished;
	}
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	public boolean isCancelled() {
		return cancelled;
	}
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	public boolean isWillRestart() {
		return willRestart;
	}
	public void setWillRestart(boolean willRestart) {
		this.willRestart = willRestart;
	}
	public String getStage() {
		return stage;
	}
	public void setStage(String stage) {
		this.stage = stage;
	}
	public List<ModuleModel> getResults() {
		return results;
	}
	public void setResults(List<ModuleModel> results) {
		this.results = results;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}

}
