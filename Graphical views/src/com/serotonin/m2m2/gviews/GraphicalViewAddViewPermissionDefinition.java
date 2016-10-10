/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Phillip Dunlap
 */
package com.serotonin.m2m2.gviews;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.m2m2.module.PermissionDefinition;

/**
 * @author Phillip Dunlap
 *
 */
public class GraphicalViewAddViewPermissionDefinition extends PermissionDefinition {

	public static final String PERMISSION = "graphical.view.addView";
	
	@Override
	public String getPermissionKey() {
		return "graphic.permission.addView";
	}

	@Override
	public String getPermissionTypeName() {
		return PERMISSION;
	}
	
	@Override
	public List<String> getDefaultGroups() {
		List<String> groups = new ArrayList<String>();
		groups.add("user");
		return groups;
	}

}
