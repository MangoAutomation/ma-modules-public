/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved. 
 * @author Phillip Dunlap
 */
package com.serotonin.m2m2.pointLinks.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

public class Upgrade3 extends DBUpgrade {

    @Override
    protected void upgrade() throws Exception {
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysql);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), sql);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), sql);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), postgres);
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "4";
    }

    private final String[] sql = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN logSize DOUBLE;",
            "ALTER TABLE pointLinks ADD COLUMN logCount INT;",
            "UPDATE pointLinks SET logSize=1, logCount=5;",
            "ALTER TABLE pointLinks ALTER COLUMN logSize DOUBLE NOT NULL;",
            "ALTER TABLE pointLinks ALTER COLUMN logCount DOUBLE NOT NULL;"
    };

    private final String[] mysql = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN logSize DOUBLE;",
            "ALTER TABLE pointLinks ADD COLUMN logCount INT;",
            "UPDATE pointLinks SET logSize=1, logCount=5;",
            "ALTER TABLE pointLinks MODIFY COLUMN logSize DOUBLE NOT NULL;",
            "ALTER TABLE pointLinks MODIFY COLUMN logCount DOUBLE NOT NULL;"
    };
    
    private final String[] postgres = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN logSize DOUBLE;",
            "ALTER TABLE pointLinks ADD COLUMN logCount INT;",
            "UPDATE pointLinks SET logSize=1, logCount=5;",
            "ALTER TABLE pointLinks ALTER COLUMN logSize SET NOT NULL;",
            "ALTER TABLE pointLinks ALTER COLUMN logCount SET NOT NULL;"
    };
}
