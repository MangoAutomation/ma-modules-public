/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.Upgrade29;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.vo.role.Role;

/**
 *
 * Move toggle permissions column to roles
 *
 * @author Terry Packer
 */
public class Upgrade3 extends Upgrade29 {

    @Override
    protected void upgrade() throws Exception {


        //Convert permissions into roles
        Map<String, Role> roles = getExistingRoles();
        //Move current permissions to roles
        ejt.query("SELECT id, togglePermission FROM advancedSchedules", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int voId = rs.getInt(1);
                //Add role/mapping
                Set<String> togglePermissions = explodePermissionGroups(rs.getString(2));
                insertMapping(voId, MaintenanceEventVO.class.getSimpleName(), "TOGGLE", togglePermissions, roles);
            }
        });

        Map<String, String[]> scripts = new HashMap<>();
        //Drop the togglePermissions
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), dropTogglePermission);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), dropTogglePermission);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), dropTogglePermission);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), dropTogglePermission);
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "4";
    }

    private final String[] dropTogglePermission = new String[] {
            "ALTER TABLE maintenanceEvents DROP COLUMN togglePermission;"
    };

}
