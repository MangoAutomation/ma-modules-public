package com.serotonin.m2m2.watchlist.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

public class Upgrade3 extends DBUpgrade {
    //private static final Log LOG = LogFactory.getLog(Upgrade3.class);

    @Override
    protected void upgrade() throws Exception {
        // Run the script.
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
        return "4";
    }

    private final String[] mssqlScript = { //
    	"ALTER TABLE watchLists ADD COLUMN type NVARCHAR(20);", //
    	"ALTER TABLE watchLists ADD COLUMN query NVARCHAR(255);", //
    };

    private final String[] mysqlScript = { //
    	"ALTER TABLE watchLists ADD COLUMN type VARCHAR(20);", //
        "ALTER TABLE watchLists ADD COLUMN query VARCHAR(255);", //
    };

}
