/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.infiniteautomation.mango.spring.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.db.query.ConditionSortLimit;
import com.infiniteautomation.mango.db.tables.DataPoints;
import com.infiniteautomation.mango.db.tables.MintermsRoles;
import com.infiniteautomation.mango.db.tables.PermissionsMinterms;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.LazyInitializer;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.AbstractVoDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.role.Role;
import com.serotonin.m2m2.watchlist.AuditEvent;
import com.serotonin.m2m2.watchlist.WatchListParameter;
import com.serotonin.m2m2.watchlist.WatchListVO;
import com.serotonin.m2m2.watchlist.db.tables.WatchListPoints;
import com.serotonin.m2m2.watchlist.db.tables.WatchLists;
import com.serotonin.m2m2.watchlist.db.tables.records.WatchListsRecord;

/**
 * @author Matthew Lohbihler
 * @author Terry Packer
 */
@Repository
public class WatchListDao extends AbstractVoDao<WatchListVO, WatchListsRecord, WatchLists> {

    private static final LazyInitializer<WatchListDao> springInstance = new LazyInitializer<>();

    private final DataPointDao dataPointDao;
    private final PermissionService permissionService;
    private final DataPoints dataPoints;
    private final WatchListPoints watchListPoints;

    @Autowired
    private WatchListDao(DataPointDao dataPointDao,
                         @Qualifier(MangoRuntimeContextConfiguration.DAO_OBJECT_MAPPER_NAME) ObjectMapper mapper,
                         ApplicationEventPublisher publisher,
                         PermissionService permissionService) {
        super(AuditEvent.TYPE_NAME, WatchLists.WATCH_LISTS,
                new TranslatableMessage("internal.monitor.WATCHLIST_COUNT"),
                mapper, publisher);
        this.dataPointDao = dataPointDao;
        this.permissionService = permissionService;
        this.dataPoints = DataPoints.DATA_POINTS;
        this.watchListPoints = WatchListPoints.WATCH_LIST_POINTS;
    }

    /**
     * Get cached instance from Spring Context
     * @return
     */
    public static WatchListDao getInstance() {
        return springInstance.get(() -> {
            Object o = Common.getRuntimeContext().getBean(WatchListDao.class);
            if(o == null)
                throw new ShouldNeverHappenException("DAO not initialized in Spring Runtime Context");
            return (WatchListDao)o;
        });
    }

    /**
     * Get the Data Points for a Given Watchlist
     * @param watchListId
     * @param callback
     */
    public void getPoints(int watchListId, final Consumer<DataPointVO> callback) {
        List<Field<?>> selectFields = dataPointDao.getSelectFields();
        selectFields.add(watchListPoints.sortOrder);

        dataPointDao.joinTables(dataPointDao.getSelectQuery(selectFields), null)
                .join(watchListPoints)
                .on(watchListPoints.dataPointId.eq(dataPoints.id))
                .where(watchListPoints.watchListId.eq(watchListId))
                .orderBy(watchListPoints.sortOrder).forEach(record -> {
            DataPointVO vo = dataPointDao.mapRecord(record);
            dataPointDao.loadRelationalData(vo);
            callback.accept(vo);
        });
    }

    @Override
    public void savePreRelationalData(WatchListVO existing, WatchListVO vo) {
        MangoPermission readPermission = permissionService.findOrCreate(vo.getReadPermission());
        vo.setReadPermission(readPermission);

        MangoPermission editPermission = permissionService.findOrCreate(vo.getEditPermission());
        vo.setEditPermission(editPermission);
    }

    @Override
    public void saveRelationalData(WatchListVO existing, WatchListVO vo) {
        if(existing != null) {
            ejt.update("DELETE FROM watchListPoints WHERE watchListId=?", vo.getId());
        }
        if(WatchListVO.STATIC_TYPE.equals(vo.getType())) {
            ejt.batchUpdate("INSERT INTO watchListPoints VALUES (?,?,?)", new InsertPoints(vo));
        }
        if(existing != null) {
            if(!existing.getReadPermission().equals(vo.getReadPermission())) {
                permissionService.deletePermissions(existing.getReadPermission());
            }
            if(!existing.getEditPermission().equals(vo.getEditPermission())) {
                permissionService.deletePermissions(existing.getEditPermission());
            }
        }
    }

    @Override
    public void loadRelationalData(WatchListVO vo) {
        //Populate permissions
        vo.setReadPermission(permissionService.get(vo.getReadPermission().getId()));
        vo.setEditPermission(permissionService.get(vo.getEditPermission().getId()));
    }

    @Override
    public void deletePostRelationalData(WatchListVO vo) {
        //Clean permissions
        permissionService.deletePermissions(vo.getReadPermission(), vo.getEditPermission());
    }

    @Override
    public <R extends Record> SelectJoinStep<R> joinPermissions(SelectJoinStep<R> select, ConditionSortLimit conditions,
            PermissionHolder user) {
        //Join on permissions
        if(!permissionService.hasAdminRole(user)) {
            List<Integer> roleIds = permissionService.getAllInheritedRoles(user).stream().map(Role::getId).collect(Collectors.toList());

            Condition roleIdsIn = MintermsRoles.MINTERMS_ROLES.roleId.in(roleIds);

            Table<?> mintermsGranted = this.create.select(MintermsRoles.MINTERMS_ROLES.mintermId)
                    .from(MintermsRoles.MINTERMS_ROLES)
                    .groupBy(MintermsRoles.MINTERMS_ROLES.mintermId)
                    .having(DSL.count().eq(DSL.count(
                            DSL.case_().when(roleIdsIn, DSL.inline(1))
                            .else_(DSL.inline((Integer)null))))).asTable("mintermsGranted");

            Table<?> permissionsGranted = this.create.selectDistinct(PermissionsMinterms.PERMISSIONS_MINTERMS.permissionId)
                    .from(PermissionsMinterms.PERMISSIONS_MINTERMS)
                    .join(mintermsGranted).on(mintermsGranted.field(MintermsRoles.MINTERMS_ROLES.mintermId).eq(PermissionsMinterms.PERMISSIONS_MINTERMS.mintermId))
                    .asTable("permissionsGranted");

            select = select.join(permissionsGranted).on(
                    permissionsGranted.field(PermissionsMinterms.PERMISSIONS_MINTERMS.permissionId).in(
                            table.readPermissionId));
        }
        return select;
    }

    private static class InsertPoints implements BatchPreparedStatementSetter {
        WatchListVO vo;

        InsertPoints(WatchListVO vo) {
            this.vo = vo;
        }

        @Override
        public int getBatchSize() {
            return vo.getPointList().size();
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setInt(1, vo.getId());
            ps.setInt(2, vo.getPointList().get(i).getId());
            ps.setInt(3, i);
        }
    }

    @Override
    protected String getXidPrefix() {
        return WatchListVO.XID_PREFIX;
    }

    @Override
    protected Record voToObjectArray(WatchListVO vo) {
        String jsonData = null;
        try{
            WatchListDbDataModel2 data = new WatchListDbDataModel2();
            data.query = vo.getQuery();
            data.params = vo.getParams();
            data.data = vo.getData();
            jsonData =  this.getObjectWriter(WatchListDbDataModel2.class).writeValueAsString(data);
        }catch(JsonProcessingException e){
            LOG.error(e.getMessage(), e);
        }

        WatchListsRecord record = table.newRecord();
        record.set(table.xid, vo.getXid());
        record.set(table.name, vo.getName());
        record.set(table.type, vo.getType());
        record.set(table.data, jsonData);
        record.set(table.readPermissionId, vo.getReadPermission().getId());
        record.set(table.editPermissionId, vo.getEditPermission().getId());
        return record;
    }

    @Override
    public WatchListVO mapRecord(Record record) {
        int i = 0;
        WatchListVO wl = new WatchListVO();
        wl.setId(record.get(table.id));
        wl.setXid(record.get(table.xid));
        wl.setName(record.get(table.name));
        wl.setType(record.get(table.type));
        //Read the data
        try{
            String c = record.get(table.data);
            if(c != null) {
                WatchListDbDataModel data = getObjectReader(WatchListDbDataModel.class).readValue(c);
                if (data != null) {
                    wl.setQuery(data.query);
                    wl.setParams(data.params);
                    wl.setData(data.data);
                }
            }
        }catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        wl.setReadPermission(new MangoPermission(record.get(table.readPermissionId)));
        wl.setEditPermission(new MangoPermission(record.get(table.editPermissionId)));
        return wl;
    }

    @JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="version", defaultImpl=WatchListDbDataModel1.class)
    @JsonSubTypes({
        @Type(name = "1", value = WatchListDbDataModel1.class),
        @Type(name = "2", value = WatchListDbDataModel2.class)
    })
    private static abstract class WatchListDbDataModel {
        @JsonProperty
        String query;
        @JsonProperty
        List<WatchListParameter> params;
        @JsonProperty
        Map<String, Object> data;
    }

    private static class WatchListDbDataModel1 extends WatchListDbDataModel {
        @JsonProperty
        List<Integer> folderIds;
    }

    private static class WatchListDbDataModel2 extends WatchListDbDataModel {

    }
}
