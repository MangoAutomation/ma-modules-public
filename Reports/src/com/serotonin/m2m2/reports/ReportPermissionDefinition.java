/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports;

import java.util.Collections;
import java.util.List;

import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.vo.permission.Permissions;

/**
 * @author Terry Packer
 *
 */
public class ReportPermissionDefinition extends PermissionDefinition{

    public static final String PERMISSION = "reports.view";

    @Override
    public String getPermissionKey() {
        return "reports.permission.view";
    }

    @Override
    public String getPermissionTypeName() {
        return PERMISSION;
    }

    @Override
    public List<String> getDefaultGroups() {
        return Collections.singletonList(Permissions.USER_DEFAULT);
    }
}
