/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import java.util.List;

import com.serotonin.m2m2.module.DatabaseSchemaDefinition;

public class ScheduledEventsSchemaDefinition extends DatabaseSchemaDefinition {

    @Override
    public String getNewInstallationCheckTableName() {
        return "scheduledEvents";
    }

    @Override
    public void addConversionTableNames(List<String> tableNames) {
        tableNames.add("scheduledEvents");
    }

    @Override
    public String getUpgradePackage() {
        return "com.serotonin.m2m2.scheduledEvents.upgrade";
    }

    @Override
    public int getDatabaseSchemaVersion() {
        return 2;
    }
}
