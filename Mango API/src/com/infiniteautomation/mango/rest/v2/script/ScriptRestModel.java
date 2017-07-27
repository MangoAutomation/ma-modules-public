package com.infiniteautomation.mango.rest.v2.script;

import java.util.List;

public class ScriptRestModel {
	private List<ScriptContextVariableModel> context;
	private String script;
	private ScriptPermissionsModel permissions;
	private String logLevel;
	
	public List<ScriptContextVariableModel> getContext() {
		return context;
	}
	public void setContext(List<ScriptContextVariableModel> context) {
		this.context = context;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	public ScriptPermissionsModel getPermissions() {
		return permissions;
	}
	public void setPermissions(ScriptPermissionsModel permissions) {
		this.permissions = permissions;
	}
	public String getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
}
