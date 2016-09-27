/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource;

import java.util.Set;

/**
 * @author Terry Packer
 *
 */
public class DataSourceSummary {

	private int id;
	private String xid;
	private Set<String> editPermissions;
	
	public DataSourceSummary(int id, String xid, Set<String> editPermissions){
		this.id = id;
		this.xid = xid;
		this.editPermissions = editPermissions;			
	}

	public int getId() {
		return id;
	}

	public String getXid() {
		return xid;
	}

	public Set<String> getEditPermissions() {
		return editPermissions;
	}
	
}
