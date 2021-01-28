package com.serotonin.m2m2.watchlist.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.db.DatabaseType;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

public class Upgrade3 extends DBUpgrade {
    //private static final Log LOG = LogFactory.getLog(Upgrade3.class);

    @Override
    protected void upgrade() throws Exception {
        // Run the script.
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseType.DERBY.name(), derbyScript);
        scripts.put(DatabaseType.MYSQL.name(), mysqlScript);
        scripts.put(DatabaseType.MSSQL.name(), mssqlScript);
        scripts.put(DatabaseType.H2.name(), mysqlScript);
        scripts.put(DatabaseType.POSTGRES.name(), mysqlScript);
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "4";
    }

    private final String[] derbyScript = {
    	"ALTER TABLE watchLists ADD COLUMN type VARCHAR(20);",
    	"ALTER TABLE watchLists ADD COLUMN data CLOB;",
        "UPDATE watchLists SET type = 'static';"
    };

    private final String[] mysqlScript = {
    	"ALTER TABLE watchLists ADD COLUMN type VARCHAR(20);",
    	"ALTER TABLE watchLists ADD COLUMN data LONGTEXT;",
        "UPDATE watchLists SET type = 'static';"
    };

    private final String[] mssqlScript = {
    	"ALTER TABLE watchLists ADD COLUMN type NVARCHAR(20);",
    	"ALTER TABLE watchLists ADD COLUMN data NTEXT;",
        "UPDATE watchLists SET type = 'static';"
    };

}
