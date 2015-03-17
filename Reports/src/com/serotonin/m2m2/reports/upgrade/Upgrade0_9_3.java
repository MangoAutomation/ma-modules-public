package com.serotonin.m2m2.reports.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

public class Upgrade0_9_3 extends DBUpgrade {
    @Override
    protected void upgrade() throws Exception {
        Map<String, String[]> scripts = new HashMap<String, String[]>();
        scripts.put(DatabaseProxy.DatabaseType.DERBY.name(), new String[] { //
                "alter table reportInstancePoints add deviceName varchar(40) default '' not null;", //
                        "update reportInstancePoints set deviceName=dataSourceName;", //
                        "alter table reportInstancePoints drop column dataSourceName;", //
                });
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), new String[] { //
                "alter table reportInstancePoints change dataSourceName deviceName varchar(40) not null;", //
                });
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), new String[] { //
                "alter table reportInstancePoints add deviceName nvarchar(40) default '' not null;", //
                        "update reportInstancePoints set deviceName=dataSourceName;", //
                        "alter table reportInstancePoints drop column dataSourceName;", //
                });
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "0.9.4";
    }
}
