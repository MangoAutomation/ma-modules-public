/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.dataImport;

import java.util.Collections;
import java.util.List;

import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.module.definitions.permissions.SuperadminPermissionDefinition;

/**
 * @author Terry Packer
 *
 */
public class DataImportPermissionDefinition extends PermissionDefinition{

    public static final String PERMISSION = "dataImport.view";

    @Override
    public String getPermissionKey() {
        return "dataImport.permission.view";
    }

    @Override
    public String getPermissionTypeName() {
        return PERMISSION;
    }

    @Override
    public List<String> getDefaultGroups() {
        return Collections.singletonList(SuperadminPermissionDefinition.GROUP_NAME);
    }
}