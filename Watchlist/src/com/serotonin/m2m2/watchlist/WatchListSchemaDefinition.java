/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import java.util.List;

import com.serotonin.m2m2.module.DatabaseSchemaDefinition;

public class WatchListSchemaDefinition extends DatabaseSchemaDefinition {

    @Override
    public String getNewInstallationCheckTableName() {
        return "watchLists";
    }

    @Override
    public void addConversionTableNames(List<String> tableNames) {
        tableNames.add("watchLists");
        tableNames.add("watchListPoints");
        tableNames.add("selectedWatchList");
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
