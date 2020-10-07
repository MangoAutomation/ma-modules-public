/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents.upgrade;

import static com.serotonin.m2m2.db.dao.tables.MintermMappingTable.MINTERMS_MAPPING;
import static com.serotonin.m2m2.db.dao.tables.PermissionMappingTable.PERMISSIONS_MAPPING;
import static com.serotonin.m2m2.db.dao.tables.PermissionTable.PERMISSIONS;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.SelectSeekStep2;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.support.TransactionTemplate;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.permission.MangoPermission.MangoPermissionBuilder;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.upgrade.DBUpgrade;
import com.serotonin.m2m2.db.upgrade.PermissionMigration;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;
import com.serotonin.m2m2.vo.role.Role;

/**
 *
 * Assign permissions to already raised Maintenance Events
 *
 * @author Terry Packer
 */
public class Upgrade4 extends DBUpgrade implements PermissionMigration {

    private final Map<MangoPermission, MangoPermission> permissionCache = new HashMap<>();
    private final Map<Role, Role> roleCache = new HashMap<>();

    private final Map<Integer, Integer> maintenanceEventPermissionMap = new HashMap<>();
    private final Map<Integer, MangoPermission> permissionMap = new HashMap<>();

    private Map<Integer, List<Integer>> eventDataPointsMap = new HashMap<>();
    private Map<Integer, MangoPermission> dataPointPermissionMap = new HashMap<>();
    private Map<Integer, List<Integer>> eventDataSourcesMap = new HashMap<>();
    private Map<Integer, MangoPermission> dataSourcePermissionMap = new HashMap<>();

    private final String SELECT_POINT_IDS = "SELECT dataPointId FROM maintenanceEventDataPoints WHERE maintenanceEventId=?";
    private final String SELECT_DATA_SOURCE_IDS = "SELECT dataSourceId FROM maintenanceEventDataSources WHERE maintenanceEventId=?";

    private final Name roleTableAlias = DSL.name("r");
    private final Table<?> roleTableAsAlias = DSL.table("roles").as(roleTableAlias);
    private final Field<Integer> roleTableIdAlias = DSL.field(roleTableAlias.append("id"), SQLDataType.INTEGER.nullable(false).identity(true));
    private final Field<String> roleTableXidAlias = DSL.field(roleTableAlias.append("xid"), SQLDataType.VARCHAR(100).nullable(false));

    @Override
    protected void upgrade() throws Exception {
        try(OutputStream out = createUpdateLogOutputStream()) {
            //Get a reference to the superadmin permission in DB
            MangoPermission superadmin = getOrCreatePermissionNoCache(MangoPermission.superadminOnly());

            ejt.query("SELECT id, typeName, typeRef1 FROM events WHERE typeName=?", new Object[] {MaintenanceEventType.TYPE_NAME},  rs -> {
                int eventId = rs.getInt(1);
                Integer voId = rs.getInt(3);

                //Find the permission id for this me
                Integer permissionId = maintenanceEventPermissionMap.computeIfAbsent(voId, (k) -> {
                    //Build the permission for this event
                    MangoPermissionBuilder builder = MangoPermission.builder();

                    List<Integer> dataPointIds = queryForList(SELECT_POINT_IDS, new Object[] {k}, Integer.class);
                    for(Integer dpId : dataPointIds) {
                        MangoPermission dataPointPermission = dataPointPermissionMap.computeIfAbsent(dpId, (pointId) -> {
                            Integer id = ejt.queryForInt("SELECT readPermissionId from dataPoints where id=?", new Object[] {pointId}, Common.NEW_ID);
                            if(id == Common.NEW_ID) {
                                return superadmin;
                            }else {
                                return permissionMap.computeIfAbsent(id, (pId) -> {
                                    MangoPermission p = get(pId);
                                    if(p == null) {
                                        return superadmin;
                                    }else {
                                        return p;
                                    }
                                });
                            }
                        });

                        dataPointPermission.getRoles().stream().forEach(minterm -> builder.minterm(minterm.stream()));
                    }

                    List<Integer> dataSourceIds = queryForList(SELECT_DATA_SOURCE_IDS, new Object[] {k}, Integer.class);
                    for(Integer dsId : dataSourceIds) {
                        MangoPermission dataSourcePermission = dataSourcePermissionMap.computeIfAbsent(dsId, (sourceId) -> {
                            Integer id = ejt.queryForInt("SELECT readPermissionId from dataSources where id=?", new Object[] {sourceId}, Common.NEW_ID);
                            if(id == Common.NEW_ID) {
                                return superadmin;
                            }else {
                                return permissionMap.computeIfAbsent(id, (pId) -> {
                                    MangoPermission p = get(pId);
                                    if(p == null) {
                                        return superadmin;
                                    }else {
                                        return p;
                                    }
                                });
                            }
                        });

                        dataSourcePermission.getRoles().stream().forEach(minterm -> builder.minterm(minterm.stream()));
                    }


                    //TODO return the new permission id
                    return getOrCreatePermission(builder.build()).getId();
                });
                ejt.update("UPDATE events SET readPermissionId=? WHERE id=?", permissionId, eventId);
            });
        }
    }

    @Override
    protected String getNewSchemaVersion() {
        return "5";
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

    public MangoPermission get(Integer id) {
        //TODO Mango 4.0 improve performance
        //Fist check to see if it exists as it may have no minterms
        Integer foundId = create.select(PERMISSIONS.id).from(PERMISSIONS).where(PERMISSIONS.id.equal(id)).fetchOneInto(Integer.class);
        if(foundId == null) {
            return null;
        }

        List<Field<?>> fields = new ArrayList<>();
        fields.add(roleTableIdAlias);
        fields.add(roleTableXidAlias);
        fields.add(PERMISSIONS_MAPPING.mintermId);

        SelectSeekStep2<Record, Integer, Integer> select = create.select(fields).from(PERMISSIONS_MAPPING)
                .join(MINTERMS_MAPPING).on(PERMISSIONS_MAPPING.mintermId.eq(MINTERMS_MAPPING.mintermId))
                .join(roleTableAsAlias).on(roleTableIdAlias.eq(MINTERMS_MAPPING.roleId))
                .where(PERMISSIONS_MAPPING.permissionId.eq(id))
                .orderBy(PERMISSIONS_MAPPING.permissionId.asc(), PERMISSIONS_MAPPING.mintermId.asc());

        String sql = select.getSQL();
        List<Object> arguments = select.getBindValues();
        Object[] argumentsArray = arguments.toArray(new Object[arguments.size()]);

        return this.query(sql, argumentsArray, new ResultSetExtractor<MangoPermission>() {

            private int roleIdIndex = 1;
            private int roleXidIndex = 2;
            private int minterIdIndex = 3;
            @Override
            public MangoPermission extractData(ResultSet rs)
                    throws SQLException, DataAccessException {

                if(rs.next()) {
                    Set<Set<Role>> roleSet = new HashSet<>();
                    Set<Role> minTerm = new HashSet<>();
                    roleSet.add(minTerm);
                    minTerm.add(new Role(rs.getInt(roleIdIndex), rs.getString(roleXidIndex)));

                    int mintermId = rs.getInt(minterIdIndex);
                    while(rs.next()) {
                        if(rs.getInt(minterIdIndex) == mintermId) {
                            //Add to current minterm
                            minTerm.add(new Role(rs.getInt(roleIdIndex), rs.getString(roleXidIndex)));
                        }else {
                            //Add to next minterm
                            minTerm = new HashSet<>();
                            roleSet.add(minTerm);
                            minTerm.add(new Role(rs.getInt(roleIdIndex), rs.getString(roleXidIndex)));
                            mintermId = rs.getInt(minterIdIndex);
                        }
                    }
                    MangoPermission permission = new MangoPermission(id, roleSet);
                    return permission;
                }else {
                    return new MangoPermission(id);
                }
            }

        });
    }
}
