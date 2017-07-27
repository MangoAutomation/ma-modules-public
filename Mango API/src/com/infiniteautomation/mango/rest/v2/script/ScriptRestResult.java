/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.script;

import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

public class ScriptRestResult {
	private String scriptOutput;
	private PointValueTimeModel scriptResult;
	
	public ScriptRestResult(String scriptOutput, PointValueTimeModel scriptResult) {
		this.scriptOutput = scriptOutput;
		this.scriptResult = scriptResult;
	}
	
	public String getScriptOutput() {
		return scriptOutput;
	}
	public void setScriptOutput(String scriptOutput) {
		this.scriptOutput = scriptOutput;
	}
	public PointValueTimeModel getScriptResult() {
		return scriptResult;
	}
	public void setScriptResult(PointValueTimeModel scriptResult) {
		this.scriptResult = scriptResult;
	}
}
