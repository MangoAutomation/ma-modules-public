/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.script;

public class ScriptContextVariableModel {
	private String xid;
	private String variableName;
	
	public ScriptContextVariableModel() { }
	
	public ScriptContextVariableModel(String xid, String variableName) {
	    this.xid = xid;
	    this.variableName = variableName;
	}
	
	public String getXid() {
		return xid;
	}
	public void setXid(String xid) {
		this.xid = xid;
	}
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
}
