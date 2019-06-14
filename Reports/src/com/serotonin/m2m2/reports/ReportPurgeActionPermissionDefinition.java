/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.reports;

import com.serotonin.m2m2.module.PermissionDefinition;

/**
 *
 * @author Terry Packer
 */
public class ReportPurgeActionPermissionDefinition extends PermissionDefinition{

    public static final String PERMISSION = "action.reportPurge";

    @Override
    public String getPermissionKey() {
        return "reports.permission.purge";
    }

    @Override
    public String getPermissionTypeName() {
        return PERMISSION;
    }

}
