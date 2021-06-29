/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.serotonin.m2m2.watchlist.upgrade;

import java.io.OutputStream;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.transaction.support.TransactionTemplate;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.DatabaseType;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;
import com.serotonin.m2m2.db.upgrade.PermissionMigration;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.role.Role;

/**
 * Remove owner from watchlist
 * @author Terry Packer
 */
public class Upgrade7 extends DBUpgrade implements PermissionMigration {
    private final Map<MangoPermission, MangoPermission> permissionCache = new HashMap<>();
    private final Map<Role, Role> roleCache = new HashMap<>();

    @Override
    protected void upgrade() throws Exception {
        try(OutputStream out = createUpdateLogOutputStream()) {

            //Update advancedSchedules
            ejt.query("SELECT id, name, userId, readPermissionId, editPermissionId FROM watchLists", rs -> {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                int userId = rs.getInt(3);
                int readPermissionId = rs.getInt(4);
                int editPermissionId = rs.getInt(5);

                //Is this user non superadmin
                AtomicBoolean isAdmin = new AtomicBoolean();
                ejt.query("SELECT roleId FROM userRoleMappings WHERE userId=?", new Object[] {userId}, row -> {
                    if(row.getInt(1) == PermissionHolder.SUPERADMIN_ROLE.getId()) {
                        isAdmin.set(true);
                    }
                });
                if(!isAdmin.get()) {
                    //Create read role
                    String readXid = UUID.randomUUID().toString();
                    String readRoleName = new TranslatableMessage("watchLists.watchListReadRolePrefix", name).translate(Common.getTranslations());
                    int readRoleId = ejt.doInsert("INSERT INTO roles (xid, name) VALUES (?, ?)",
                            new Object[]{readXid, readRoleName},
                            new int[]{Types.VARCHAR, Types.VARCHAR});
                    Role readRole = new Role(readRoleId, readXid);

                    //Assign to user
                    ejt.doInsert("INSERT INTO userRoleMappings (roleId, userId) VALUES (?,?)",
                            new Object[] {readRoleId, userId},
                            new int[] {Types.INTEGER, Types.INTEGER});

                    //Create read permission
                    MangoPermission readPermission = getExistingPermission(readPermissionId);
                    if(readPermission == null) {
                        readPermission = new MangoPermission();
                    }
                    Set<Set<Role>> readRoles = new HashSet<>(readPermission.getRoles());
                    readRoles.add(Collections.singleton(readRole));
                    MangoPermission newReadPermission = getOrCreatePermission(new MangoPermission(readRoles));

                    //Create edit role
                    String editXid = UUID.randomUUID().toString();
                    String editRoleName = new TranslatableMessage("watchLists.watchListEditRolePrefix", name).translate(Common.getTranslations());
                    int editRoleId = ejt.doInsert("INSERT INTO roles (xid, name) VALUES (?, ?)",
                            new Object[]{editXid, editRoleName},
                            new int[]{Types.VARCHAR, Types.VARCHAR});
                    Role editRole = new Role(editRoleId, editXid);

                    //Assign to user
                    ejt.doInsert("INSERT INTO userRoleMappings (roleId, userId) VALUES (?,?)",
                            new Object[] {editRoleId, userId},
                            new int[] {Types.INTEGER, Types.INTEGER});

                    //Create edit permission
                    MangoPermission editPermission = getExistingPermission(editPermissionId);
                    if(editPermission == null) {
                        editPermission = new MangoPermission();
                    }
                    Set<Set<Role>> editRoles = new HashSet<>(editPermission.getRoles());
                    editRoles.add(Collections.singleton(editRole));
                    MangoPermission newEditPermission = getOrCreatePermission(new MangoPermission(editRoles));

                    //Update the permissionIds
                    ejt.update("UPDATE watchLists SET readPermissionId=?, editPermissionId=? WHERE id=?", new Object[] {newReadPermission.getId(), newEditPermission.getId(), id});
                }
            });

            //Drop the columns and indexes
            Map<String, String[]> scripts = new HashMap<>();
            scripts.put(DatabaseType.MYSQL.name(), mySQL);
            scripts.put(DatabaseType.H2.name(), sql);
            scripts.put(DatabaseType.MSSQL.name(), sql);
            scripts.put(DatabaseType.POSTGRES.name(), mySQL);
            runScript(scripts, out);
        }
    }

    private final String[] mySQL = new String[] {
            "ALTER TABLE watchLists DROP FOREIGN KEY watchListsFk1;",
            "ALTER TABLE watchLists DROP INDEX watchListsFk1;",
            "ALTER TABLE watchLists DROP COLUMN userId;"
    };

    private final String[] sql = new String[] {
            "ALTER TABLE watchLists DROP COLUMN userId;",
    };

    @Override
    protected String getNewSchemaVersion() {
        return "8";
    }

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
