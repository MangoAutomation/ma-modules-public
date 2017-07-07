package com.infiniteautomation.mango.rest.v2.script;

import com.serotonin.m2m2.rt.script.ScriptPermissions;

public class ScriptPermissionsModel {
	private String dataPointReadPermissions;
	private String dataPointSetPermissions;
	private String dataSourcePermissions;
	
	public String getDataPointReadPermissions() {
		return dataPointReadPermissions;
	}
	public void setDataPointReadPermissions(String dataPointReadPermissions) {
		this.dataPointReadPermissions = dataPointReadPermissions;
	}
	public String getDataPointSetPermissions() {
		return dataPointSetPermissions;
	}
	public void setDataPointSetPermissions(String dataPointSetPermissions) {
		this.dataPointSetPermissions = dataPointSetPermissions;
	}
	public String getDataSourcePermissions() {
		return dataSourcePermissions;
	}
	public void setDataSourcePermissions(String dataSourcePermissions) {
		this.dataSourcePermissions = dataSourcePermissions;
	}
	
	public ScriptPermissions toPermissions() {
		ScriptPermissions permissions = new ScriptPermissions();
		permissions.setDataPointReadPermissions(dataPointReadPermissions);
		permissions.setDataPointSetPermissions(dataPointSetPermissions);
		permissions.setDataSourcePermissions(dataSourcePermissions);
		return permissions;
	}
}
