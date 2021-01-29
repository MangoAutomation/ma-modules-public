/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.util.List;

import org.jooq.Table;

import com.serotonin.m2m2.module.DatabaseSchemaDefinition;
import com.serotonin.m2m2.watchlist.db.DefaultSchema;

public class WatchListSchemaDefinition extends DatabaseSchemaDefinition {

    @Override
    public String getNewInstallationCheckTableName() {
        return "watchLists";
    }

    @Override
    public List<Table<?>> getTablesForConversion() {
        return DefaultSchema.DEFAULT_SCHEMA.getTables();
    }

    @Override
    public String getUpgradePackage() {
        return "com.serotonin.m2m2.watchlist.upgrade";
    }

    @Override
    public int getDatabaseSchemaVersion() {
        return 8;
    }
}
