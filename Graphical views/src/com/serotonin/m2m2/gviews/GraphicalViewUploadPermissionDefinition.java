/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.gviews;

import com.serotonin.m2m2.module.PermissionDefinition;

/**
 * @author Terry Packer
 *
 */
public class GraphicalViewUploadPermissionDefinition extends PermissionDefinition{

	public static final String PERMISSION = "graphical.view.upload";
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.PermissionDefinition#getPermissionKey()
	 */
	@Override
	public String getPermissionKey() {
		return "graphic.permission.upload";
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.PermissionDefinition#getPermissionTypeName()
	 */
	@Override
	public String getPermissionTypeName() {
		return PERMISSION;
	}
}
