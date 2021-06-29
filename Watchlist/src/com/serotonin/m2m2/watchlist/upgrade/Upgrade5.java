/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.watchlist.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.db.DatabaseType;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

public class Upgrade5 extends DBUpgrade {

    @Override
    protected void upgrade() throws Exception {
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseType.MYSQL.name(), mysql);
        scripts.put(DatabaseType.H2.name(), h2);
        scripts.put(DatabaseType.MSSQL.name(), mssql);
        scripts.put(DatabaseType.POSTGRES.name(), postgres);
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "6";
    }

    private final String[] mysql = new String[] {
        "ALTER TABLE users MODIFY COLUMN name varchar(255);"
    };
    
    private final String[] h2 = new String[] {
        "ALTER TABLE users ALTER COLUMN name varchar(255);"
    };
    
    private final String[] mssql = new String[] {
        "ALTER TABLE users ALTER COLUMN name nvarchar(255);"
    };
    
    private final String[] postgres = new String[] {
        "ALTER TABLE users ALTER COLUMN name varchar(255);"
    };
}
