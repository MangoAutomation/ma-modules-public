/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.util.List;

import org.jooq.Table;

import com.serotonin.m2m2.maintenanceEvents.db.DefaultSchema;
import com.serotonin.m2m2.module.DatabaseSchemaDefinition;

public class SchemaDefinition extends DatabaseSchemaDefinition {

    public static final String TABLE_NAME = "maintenanceEvents";

    @Override
    public String getNewInstallationCheckTableName() {
        return TABLE_NAME;
    }

    @Override
    public List<Table<?>> getTablesForConversion() {
        return DefaultSchema.DEFAULT_SCHEMA.getTables();
    }

    @Override
    public String getUpgradePackage() {
        return "com.serotonin.m2m2.maintenanceEvents.upgrade";
    }

    @Override
    public int getDatabaseSchemaVersion() {
        return 5;
    }

}
