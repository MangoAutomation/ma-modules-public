/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.pointLinks.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

/**
 * @author Terry Packer
 *
 */
public class Upgrade1 extends DBUpgrade {
	
    @Override
    protected void upgrade() throws Exception {
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.DERBY.name(), mysqlScript);
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysqlScript);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssqlScript);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), mysqlScript);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), mysqlScript);
        runScript(scripts);

    }

    @Override
    protected String getNewSchemaVersion() {
        return "2";
    }
    
    private final String[] mssqlScript = { //
		"ALTER TABLE pointLinks ADD COLUMN logLevel NOT NULL DEFAULT 0;", //
    	"ALTER TABLE pointLinks ADD COLUMN scriptDataSourcePermission NVARCHAR(255) NOT NULL DEFAULT '';", //
        "ALTER TABLE pointLinks ADD COLUMN scriptDataPointSetPermission NVARCHAR(255) NOT NULL DEFAULT '';", //
        "ALTER TABLE pointLinks ADD COLUMN scriptDataPointReadPermission NVARCHAR(255) NOT NULL DEFAULT '';", //
    };

    private final String[] mysqlScript = { //
    	"ALTER TABLE pointLinks ADD COLUMN logLevel int NOT NULL DEFAULT 0;", //
    	"ALTER TABLE pointLinks ADD COLUMN scriptDataSourcePermission VARCHAR(255) NOT NULL DEFAULT '';", //
        "ALTER TABLE pointLinks ADD COLUMN scriptDataPointSetPermission VARCHAR(255) NOT NULL DEFAULT '';", //
        "ALTER TABLE pointLinks ADD COLUMN scriptDataPointReadPermission VARCHAR(255) NOT NULL DEFAULT '';", //
    };

}
