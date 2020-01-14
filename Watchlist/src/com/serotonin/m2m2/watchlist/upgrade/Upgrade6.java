/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.serotonin.m2m2.watchlist.upgrade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.db.upgrade.Upgrade29;
import com.serotonin.m2m2.vo.role.Role;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 *
 * @author Terry Packer
 */
public class Upgrade6 extends Upgrade29 {

    @Override
    protected void upgrade() throws Exception {

        //First drop any watch lists of type 'hierarhcy'
        ejt.update("DELETE FROM watchLists WHERE type='hierarchy'");

        //Convert permissions into roles
        Map<String, Role> roles = getExistingRoles();
        //Move current permissions to roles
        ejt.query("SELECT id, readPermission, editPermission FROM watchLists", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int voId = rs.getInt(1);
                //Add role/mapping
                Set<String> readPermissions = explodePermissionGroups(rs.getString(2));
                insertMapping(voId, WatchListVO.class.getSimpleName(), PermissionService.READ, readPermissions, roles);
                Set<String> editPermissions = explodePermissionGroups(rs.getString(3));
                insertMapping(voId, WatchListVO.class.getSimpleName(), PermissionService.EDIT, editPermissions, roles);
            }
        });

        Map<String, String[]> scripts = new HashMap<>();
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), sql);
        scripts.put(DatabaseProxy.DatabaseType.H2.name(), sql);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), sql);
        scripts.put(DatabaseProxy.DatabaseType.POSTGRES.name(), sql);
        runScript(scripts);
    }

    private final String[] sql = new String[] {
            "ALTER TABLE watchLists DROP COLUMN readPermission;",
            "ALTER TABLE watchLists DROP COLUMN editPermission;",
    };
}
