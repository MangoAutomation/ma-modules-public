/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import java.util.List;

import com.serotonin.m2m2.module.DatabaseSchemaDefinition;

public class PointLinkSchemaDefinition extends DatabaseSchemaDefinition {
    
    public static final String TABLE_NAME = "pointLinks";
    
    @Override
    public String getNewInstallationCheckTableName() {
        return TABLE_NAME;
    }

    @Override
    public void addConversionTableNames(List<String> tableNames) {
        tableNames.add(TABLE_NAME);
    }

    @Override
    public String getUpgradePackage() {
        return "com.serotonin.m2m2.pointLinks.upgrade";
    }

    @Override
    public int getDatabaseSchemaVersion() {
        return 6;
    }

}
