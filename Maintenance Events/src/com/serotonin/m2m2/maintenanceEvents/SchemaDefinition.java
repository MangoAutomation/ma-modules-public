/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.util.List;

import com.serotonin.m2m2.module.DatabaseSchemaDefinition;

public class SchemaDefinition extends DatabaseSchemaDefinition {
    
    public static final String TABLE_NAME = "maintenanceEvents";
    
    @Override
    public String getNewInstallationCheckTableName() {
        return TABLE_NAME;
    }

    @Override
    public void addConversionTableNames(List<String> tableNames) {
        tableNames.add(TABLE_NAME);
        tableNames.add("maintenanceEventDataPoints");
        tableNames.add("maintenanceEventDataSources");
    }

    @Override
    public String getUpgradePackage() {
        return "com.serotonin.m2m2.maintenanceEvents.upgrade";
    }

    @Override
    public int getDatabaseSchemaVersion() {
        return 3;
    }

}
