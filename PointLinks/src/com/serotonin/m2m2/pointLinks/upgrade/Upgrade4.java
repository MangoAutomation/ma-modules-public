/**
 * Copyright (C) 2018 Infinite Automation Systems, Inc. All rights reserved
 * 
 */
package com.serotonin.m2m2.pointLinks.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

/**
 *
 * @author Phillip Dunlap
 */
public class Upgrade4 extends DBUpgrade {

    @Override
    protected void upgrade() throws Exception {
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysql);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssql);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), postgres);
        runScript(scripts);
        
        //Add a default name of XID
        ejt.update("UPDATE pointLinks SET name = xid");
        
        scripts.clear();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysqlAlter);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2Alter);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssqlAlter);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), postgresAlter);
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "5";
    }
    
    private final String[] mysql = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN name VARCHAR(255);"
    };
    private final String[] h2 = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN name VARCHAR(255);"
    };
    private final String[] mssql = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN name NVARCHAR(255);"
    };
    private final String[] postgres = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN name TYPE VARCHAR(255);"
    };

    private final String[] mysqlAlter = new String[] {
            "ALTER TABLE pointLinks MODIFY COLUMN name VARCHAR(255) NOT NULL;"
    };
    private final String[] h2Alter = new String[] {
            "ALTER TABLE pointLinks ALTER COLUMN name VARCHAR(255) NOT NULL;"
    };
    private final String[] mssqlAlter = new String[] {
            "ALTER TABLE pointLinks ALTER COLUMN name NVARCHAR(255) NOT NULL;"
    };
    private final String[] postgresAlter = new String[] {
            "ALTER TABLE pointLinks ALTER COLUMN name TYPE VARCHAR(255) NOT NULL;"
    };
}
