/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import java.util.List;

import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.DatabaseSchemaDefinition;

public class SchemaDefinition extends DatabaseSchemaDefinition {
    @Override
    public void newInstallationCheck(ExtendedJdbcTemplate ejt) {
        if (!Common.databaseProxy.tableExists(ejt, "reports")) {
            String path = Common.MA_HOME + getModule().getDirectoryPath() + "/web/db/createTables-"
                    + Common.databaseProxy.getType().name() + ".sql";
            Common.databaseProxy.runScriptFile(path, null);
        }
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

    @Override
    public void postRuntimeManagerTerminate(boolean uninstall) {
        if(uninstall) {
            String path = Common.MA_HOME + getModule().getDirectoryPath() + "/web/db/uninstall.sql";
            Common.databaseProxy.runScriptFile(path, null);
        }
    }
}
