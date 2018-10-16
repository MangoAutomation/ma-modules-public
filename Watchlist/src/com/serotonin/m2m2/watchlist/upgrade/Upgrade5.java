/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved. 
 * @author Phillip Dunlap
 */
package com.serotonin.m2m2.watchlist.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

public class Upgrade5 extends DBUpgrade {

    @Override
    protected void upgrade() throws Exception {
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysql);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssql);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), postgres);
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
