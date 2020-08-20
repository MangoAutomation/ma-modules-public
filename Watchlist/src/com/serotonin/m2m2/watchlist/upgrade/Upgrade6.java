/*
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.serotonin.m2m2.watchlist.upgrade;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;
import com.serotonin.m2m2.db.upgrade.PermissionMigration;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Terry Packer
 */
public class Upgrade6 extends DBUpgrade implements PermissionMigration {

    @Override
    protected void upgrade() throws Exception {
        OutputStream out = createUpdateLogOutputStream();

        //First drop any watch lists of type 'hierarhcy'
        ejt.update("DELETE FROM watchLists WHERE type='hierarchy'");

        //Create permission columns
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), addPermissionsSQL);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), addPermissionsSQL);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), addPermissionsSQL);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), addPermissionsSQL);
        runScript(scripts, out);

        //Convert permissions into roles
        //Move current permissions to roles
        ejt.query("SELECT id, readPermission, editPermission FROM watchLists", rs -> {
            int voId = rs.getInt(1);
            //Add role/mapping
            MangoPermission readPermissions = PermissionMigration.parseLegacyPermission(rs.getString(2));
            Integer read = getOrCreatePermission(readPermissions).getId();
            MangoPermission editPermissions = PermissionMigration.parseLegacyPermission(rs.getString(3));
            Integer edit = getOrCreatePermission(editPermissions).getId();
            ejt.update("UPDATE watchLists SET readPermissionId=?, editPermissionId=? WHERE id=?", read, edit, voId);
        });

        //Modify permission columns
        scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), permissionsNotNullMySQL);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), permissionsNotNullSQL);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), permissionsNotNullSQL);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), permissionsNotNullSQL);
        runScript(scripts, out);

        scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), sql);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), sql);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), sql);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), sql);
        runScript(scripts, out);
    }

    private final String[] sql = new String[] {
            "ALTER TABLE watchLists DROP COLUMN readPermission;",
            "ALTER TABLE watchLists DROP COLUMN editPermission;",
    };

    private final String[] addPermissionsSQL = new String[] {
            "ALTER TABLE watchLists ADD COLUMN readPermissionId INT;",
            "ALTER TABLE watchLists ADD COLUMN editPermissionId INT;",
            "ALTER TABLE watchLists ADD CONSTRAINT watchListsFk2 FOREIGN KEY (readPermissionId) REFERENCES permissions(id) ON DELETE RESTRICT;",
            "ALTER TABLE watchLists ADD CONSTRAINT watchListsFk3 FOREIGN KEY (editPermissionId) REFERENCES permissions(id) ON DELETE RESTRICT;",
    };

    private final String[] permissionsNotNullSQL = new String[] {
            "ALTER TABLE watchLists ALTER COLUMN readPermissionId INT NOT NULL;",
            "ALTER TABLE watchLists ALTER COLUMN editPermissionId INT NOT NULL;",
    };

    private final String[] permissionsNotNullMySQL = new String[] {
            "ALTER TABLE watchLists MODIFY COLUMN readPermissionId INT NOT NULL;",
            "ALTER TABLE watchLists MODIFY COLUMN editPermissionId INT NOT NULL;",
    };

    @Override
    protected String getNewSchemaVersion() {
        return "7";
    }

    @Override
    public TransactionTemplate getTransactionTemplate() {
        return super.getTransactionTemplate();
    }

    @Override
    public ExtendedJdbcTemplate getJdbcTemplate() {
        return ejt;
    }
}
