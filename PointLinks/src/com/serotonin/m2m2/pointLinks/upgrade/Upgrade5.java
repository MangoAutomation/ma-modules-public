/**
 * Copyright (C) 2018 Infinite Automation Systems, Inc. All rights reserved
 * 
 */
package com.serotonin.m2m2.pointLinks.upgrade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;
import com.serotonin.m2m2.vo.permission.Permissions;

/**
 *
 * @author Phillip Dunlap
 */
public class Upgrade5 extends DBUpgrade {

    @Override
    protected void upgrade() throws Exception {
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysql);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssql);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), postgres);
        runScript(scripts);
        
        //Collect all the permissions and re-insert
        ejt.query("SELECT id,scriptDataSourcePermission,scriptDataPointSetPermission,scriptDataPointReadPermission FROM pointLinks", (rs) -> {
            Set<String> permissions = new HashSet<String>();
            int id = rs.getInt(1);
            permissions.addAll(Permissions.explodePermissionGroups(rs.getString(2)));
            permissions.addAll(Permissions.explodePermissionGroups(rs.getString(3)));
            permissions.addAll(Permissions.explodePermissionGroups(rs.getString(4)));
            ejt.update("UPDATE pointLinks SET scriptPermissions=? WHERE id=?", new Object[] {Permissions.implodePermissionGroups(permissions), id});
        });
        scripts.clear();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysqlDrop);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), h2Drop);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssqlDrop);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), postgresDrop);
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "6";
    }
    
    private final String[] mysql = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN scriptPermissions VARCHAR(255);"
    };
    private final String[] h2 = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN scriptPermissions VARCHAR(255);"
    };
    private final String[] mssql = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN scriptPermissions NVARCHAR(255);"
    };
    private final String[] postgres = new String[] {
            "ALTER TABLE pointLinks ADD COLUMN scriptPermissions TYPE VARCHAR(255);"
    };

    private final String[] mysqlDrop = new String[] {
            "ALTER TABLE pointLinks DROP COLUMN scriptDataSourcePermission;",
            "ALTER TABLE pointLinks DROP COLUMN scriptDataPointSetPermission;",
            "ALTER TABLE pointLinks DROP COLUMN scriptDataPointReadPermission;"
    };
    private final String[] h2Drop = new String[] {
            "ALTER TABLE pointLinks DROP COLUMN scriptDataSourcePermission;",
            "ALTER TABLE pointLinks DROP COLUMN scriptDataPointSetPermission;",
            "ALTER TABLE pointLinks DROP COLUMN scriptDataPointReadPermission;"
    };
    private final String[] mssqlDrop = new String[] {
            "ALTER TABLE pointLinks DROP COLUMN scriptDataSourcePermission;",
            "ALTER TABLE pointLinks DROP COLUMN scriptDataPointSetPermission;",
            "ALTER TABLE pointLinks DROP COLUMN scriptDataPointReadPermission;"
    };
    private final String[] postgresDrop = new String[] {
            "ALTER TABLE pointLinks DROP COLUMN scriptDataSourcePermission;",
            "ALTER TABLE pointLinks DROP COLUMN scriptDataPointSetPermission;",
            "ALTER TABLE pointLinks DROP COLUMN scriptDataPointReadPermission;"
    };
}
