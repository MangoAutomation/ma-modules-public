/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents.upgrade;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;
import com.serotonin.m2m2.db.upgrade.PermissionMigration;
import com.serotonin.m2m2.vo.role.Role;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Move toggle permissions column to roles
 *
 * @author Terry Packer
 */
public class Upgrade3 extends DBUpgrade implements PermissionMigration {
    private final Map<MangoPermission, MangoPermission> permissionCache = new HashMap<>();
    private final Map<Role, Role> roleCache = new HashMap<>();

    @Override
    protected void upgrade() throws Exception {
        OutputStream out = createUpdateLogOutputStream();

        //Create permission columns
        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), addPermissionsSQL);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), addPermissionsSQL);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), addPermissionsSQL);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), addPermissionsSQL);
        runScript(scripts, out);

        //Convert permissions into roles
        //Move current permissions to roles
        ejt.query("SELECT id, togglePermission FROM maintenanceEvents", rs -> {
            int voId = rs.getInt(1);
            //Add role/mapping
            MangoPermission togglePermissions = PermissionMigration.parseLegacyPermission(rs.getString(2));
            Integer toggle = getOrCreatePermission(togglePermissions).getId();
            ejt.update("UPDATE maintenanceEvents SET togglePermissionId=? WHERE id=?", toggle, voId);
        });

        //Modify permission columns
        scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), permissionsNotNullMySQL);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), permissionsNotNullSQL);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), permissionsNotNullSQL);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), permissionsNotNullSQL);
        runScript(scripts, out);

        scripts = new HashMap<>();
        //Drop the togglePermissions
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), dropTogglePermission);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), dropTogglePermission);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), dropTogglePermission);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), dropTogglePermission);
        runScript(scripts, out);
    }

    private final String[] addPermissionsSQL = new String[] {
            "ALTER TABLE maintenanceEvents ADD COLUMN togglePermissionId INT;",
            "ALTER TABLE maintenanceEvents ADD CONSTRAINT maintenanceEventsFk1 FOREIGN KEY (togglePermissionId) REFERENCES permissions(id) ON DELETE RESTRICT;",
    };

    private final String[] permissionsNotNullSQL = new String[] {
            "ALTER TABLE maintenanceEvents ALTER COLUMN togglePermissionId INT NOT NULL;"
    };

    private final String[] permissionsNotNullMySQL = new String[] {
            "ALTER TABLE maintenanceEvents MODIFY COLUMN togglePermissionId INT NOT NULL;",
    };

    @Override
    protected String getNewSchemaVersion() {
        return "4";
    }

    private final String[] dropTogglePermission = new String[] {
            "ALTER TABLE maintenanceEvents DROP COLUMN togglePermission;"
    };

    @Override
    public TransactionTemplate getTransactionTemplate() {
        return super.getTransactionTemplate();
    }

    @Override
    public ExtendedJdbcTemplate getJdbcTemplate() {
        return ejt;
    }

    @Override
    public Map<MangoPermission, MangoPermission> permissionCache() {
        return this.permissionCache;
    }

    @Override
    public Map<Role, Role> roleCache() {
        return this.roleCache;
    }
}
