/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.m2m2.module.PermissionDefinition;

/**
 * @author Terry Packer
 *
 */
public class WatchListPermissionDefinition extends PermissionDefinition{

	public static final String PERMISSION = "watchlist.view";
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.PermissionDefinition#getPermissionKey()
	 */
	@Override
	public String getPermissionKey() {
		return "watchList.permission.view";
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.PermissionDefinition#getPermissionTypeName()
	 */
	@Override
	public String getPermissionTypeName() {
		return PERMISSION;
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.PermissionDefinition#getDefaultGroups()
	 */
	@Override
	public List<String> getDefaultGroups() {
		List<String> groups = new ArrayList<String>();
		groups.add("user");
		return groups;
	}
}
