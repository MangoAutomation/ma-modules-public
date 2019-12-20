/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.module.PermissionDefinition;

public class StatusPermissionDef extends PermissionDefinition {
	
	public static final String PERMISSION = "internal.status";
	
    @Override
    public String getPermissionKey() {
        return "internal.metrics.permissions";
    }

    @Override
    public String getPermissionTypeName() {
        return PERMISSION;
    }
}
