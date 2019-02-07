/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Phillip Dunlap
 */
package com.serotonin.m2m2.gviews;

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

}
