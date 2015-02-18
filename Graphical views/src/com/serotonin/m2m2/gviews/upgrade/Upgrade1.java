/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.gviews.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;

/**
 * @author Terry Packer
 *
 */
public class Upgrade1 extends DBUpgrade {
    private static final Log LOG = LogFactory.getLog(Upgrade1.class);

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

        // Convert existing permissions data.
        final Map<Integer, String> readPermission = new HashMap<>();
        final Map<Integer, String> editPermission = new HashMap<>();
        ejt.query("SELECT g.graphicalViewId, g.accessType, u.username FROM graphicalViewUsers g JOIN users u ON g.userId=u.id",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        int gvId = rs.getInt(1);
                        int permission = rs.getInt(2);
                        String username = rs.getString(3);

                        if (permission == 1) // Read
                            updatePermissionString(readPermission, gvId, username);
                        else if (permission > 1) // Read, owner
                            updatePermissionString(editPermission, gvId, username);
                        else
                            LOG.warn("Unknown permission type in dataPointUsers: " + permission + ", ignored");
                    }
                });

        for (Map.Entry<Integer, String> e : readPermission.entrySet())
            ejt.update("UPDATE graphicalViews SET readPermission=? WHERE id=?", e.getValue(), e.getKey());
        for (Map.Entry<Integer, String> e : editPermission.entrySet())
            ejt.update("UPDATE graphicalViews SET editPermission=? WHERE id=?", e.getValue(), e.getKey());

        // Goodbye share table.
        scripts.put(DatabaseProxy.DatabaseType.DERBY.name(), dropScript);
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), dropScript);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), dropScript);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), dropScript);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), dropScript);
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "2";
    }

    void updatePermissionString(Map<Integer, String> map, int id, String username) {
        String permission = map.get(id);
        if (permission == null)
            permission = "";
        else
            permission += ",";
        permission += username;
        map.put(id, permission);
    }

    private final String[] mssqlScript = { //
    "ALTER TABLE graphicalViews ADD COLUMN readPermission NVARCHAR(255);", //
            "ALTER TABLE graphicalViews ADD COLUMN editPermission NVARCHAR(255);", //
    };

    private final String[] mysqlScript = { //
    "ALTER TABLE graphicalViews ADD COLUMN readPermission VARCHAR(255);", //
            "ALTER TABLE graphicalViews ADD COLUMN editPermission VARCHAR(255);", //
    };

    private final String[] dropScript = { //
    "DROP TABLE graphicalViewUsers;", //
    };
}