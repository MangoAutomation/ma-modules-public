/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import java.util.List;

import com.serotonin.m2m2.module.DatabaseSchemaDefinition;

public class SchemaDefinition extends DatabaseSchemaDefinition {
    
    @Override
    public String getNewInstallationCheckTableName() {
        return "reports";
    }
    
    @Override
    public void addConversionTableNames(List<String> tableNames) {
        tableNames.add("reports");
        tableNames.add("reportInstances");
        tableNames.add("reportInstancePoints");
        tableNames.add("reportInstanceData");
        tableNames.add("reportInstanceDataAnnotations");
        tableNames.add("reportInstanceEvents");
        tableNames.add("reportInstanceUserComments");
    }

    @Override
    public String getUpgradePackage() {
        return "com.serotonin.m2m2.reports.upgrade";
    }

    @Override
    public int getDatabaseSchemaVersion() {
        return 5;
    }
}
