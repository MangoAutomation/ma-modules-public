/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.infiniteautomation.mango.spring.dao;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
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
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.infiniteautomation.mango.spring.db.RoleTableDefinition;
import com.infiniteautomation.mango.spring.db.UserTableDefinition;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.LazyInitializer;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PermissionDao;
import com.serotonin.m2m2.db.dao.tables.MintermMappingTable;
import com.serotonin.m2m2.db.dao.tables.PermissionMappingTable;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.watchlist.AuditEvent;
import com.serotonin.m2m2.watchlist.WatchListParameter;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 * @author Matthew Lohbihler
 * @author Terry Packer
 */
@Repository
public class WatchListDao extends AbstractDao<WatchListVO, WatchListTableDefinition> {

    private static final LazyInitializer<WatchListDao> springInstance = new LazyInitializer<>();

    private final DataPointDao dataPointDao;
    private final UserTableDefinition userTable;
    private final PermissionService permissionService;
    private final PermissionDao permissionDao;

    @Autowired
    private WatchListDao(WatchListTableDefinition table, DataPointDao dataPointDao,
            UserTableDefinition userTable,
            @Qualifier(MangoRuntimeContextConfiguration.DAO_OBJECT_MAPPER_NAME)ObjectMapper mapper,
            ApplicationEventPublisher publisher,
            PermissionService permissionService,
            PermissionDao permissionDao) {
        super(AuditEvent.TYPE_NAME, table,
                new TranslatableMessage("internal.monitor.WATCHLIST_COUNT"),
                mapper, publisher);
        this.dataPointDao = dataPointDao;
        this.userTable = userTable;
        this.permissionService = permissionService;
        this.permissionDao = permissionDao;
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
    public void getPoints(int watchListId, final Consumer<DataPointVO> callback){

        SelectJoinStep<Record> selectPoints = dataPointDao.getJoinedSelectQuery();
        selectPoints.join(table.POINTS_TABLE_AS_ALIAS)
        .on(table.POINTS_DATA_POINT_ID_ALIAS.eq(dataPointDao.getTable().getAlias("id")))
        .where(table.POINTS_DATA_POINT_WATCHLIST_ID_ALIAS.eq(watchListId))
        .orderBy(table.POINTS_DATA_POINT_WATCHLIST_SORT_ORDER);

        RowMapper<DataPointVO> pointMapper = DataPointDao.getInstance().getRowMapper();
        String sql = selectPoints.getSQL();
        List<Object> args = selectPoints.getBindValues();

        this.ejt.query(sql, args.toArray(new Object[args.size()]), new RowCallbackHandler(){
            private int row = 0;
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                DataPointVO vo = pointMapper.mapRow(rs, row);
                dataPointDao.loadRelationalData(vo);
                callback.accept(vo);
                row++;
            }

        });
    }

    /*
     * Override to use select distinct due to permissions joins
     */
    @Override
    public SelectJoinStep<Record> getSelectQuery(List<Field<?>> fields) {
        return this.create.selectDistinct(fields)
                .from(this.table.getTableAsAlias());
    }

    @Override
    public void savePreRelationalData(WatchListVO existing, WatchListVO vo) {
        permissionDao.permissionId(vo.getReadPermission());
        permissionDao.permissionId(vo.getEditPermission());
    }

    @Override
    public void saveRelationalData(WatchListVO existing, WatchListVO vo) {
        if(existing != null) {
            ejt.update("DELETE FROM watchListPoints WHERE watchListId=?", new Object[] { vo.getId() });
        }
        if(WatchListVO.STATIC_TYPE.equals(vo.getType())) {
            ejt.batchUpdate("INSERT INTO watchListPoints VALUES (?,?,?)", new InsertPoints(vo));
        }
        if(existing != null) {
            if(!existing.getReadPermission().equals(vo.getReadPermission())) {
                permissionDao.permissionDeleted(existing.getReadPermission());
            }
            if(!existing.getEditPermission().equals(vo.getEditPermission())) {
                permissionDao.permissionDeleted(existing.getEditPermission());
            }
        }
    }

    @Override
    public void loadRelationalData(WatchListVO vo) {
        //Populate permissions
        vo.setReadPermission(permissionDao.get(vo.getReadPermission().getId()));
        vo.setEditPermission(permissionDao.get(vo.getEditPermission().getId()));
    }

    @Override
    public void deletePostRelationalData(WatchListVO vo) {
        //Clean permissions
        permissionDao.permissionDeleted(vo.getReadPermission(), vo.getEditPermission());
    }

    @Override
    public <R extends Record> SelectJoinStep<R> joinPermissions(SelectJoinStep<R> select, ConditionSortLimit conditions,
            PermissionHolder user) {
        //Join on permissions
        if(!permissionService.hasAdminRole(user)) {
            List<Integer> roleIds = user.getAllInheritedRoles().stream().map(r -> r.getId()).collect(Collectors.toList());

            Condition roleIdsIn = RoleTableDefinition.roleIdField.in(roleIds);

            Table<?> mintermsGranted = this.create.select(MintermMappingTable.MINTERMS_MAPPING.mintermId)
                    .from(MintermMappingTable.MINTERMS_MAPPING)
                    .groupBy(MintermMappingTable.MINTERMS_MAPPING.mintermId)
                    .having(DSL.count().eq(DSL.count(
                            DSL.case_().when(roleIdsIn, DSL.inline(1))
                            .else_(DSL.inline((Integer)null))))).asTable("mintermsGranted");

            Table<?> permissionsGranted = this.create.selectDistinct(PermissionMappingTable.PERMISSIONS_MAPPING.permissionId)
                    .from(PermissionMappingTable.PERMISSIONS_MAPPING)
                    .join(mintermsGranted).on(mintermsGranted.field(MintermMappingTable.MINTERMS_MAPPING.mintermId).eq(PermissionMappingTable.PERMISSIONS_MAPPING.mintermId))
                    .asTable("permissionsGranted");

            select = select.join(permissionsGranted).on(
                    permissionsGranted.field(PermissionMappingTable.PERMISSIONS_MAPPING.permissionId).in(
                            WatchListTableDefinition.READ_PERMISSION_ALIAS, WatchListTableDefinition.EDIT_PERMISSION_ALIAS)
                    .or(this.table.getAlias("userId").eq(((User)user).getId())));
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
    protected Object[] voToObjectArray(WatchListVO vo) {
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

        return new Object[]{
                vo.getXid(),
                vo.getName(),
                vo.getUserId(),
                vo.getType(),
                jsonData,
                vo.getReadPermission().getId(),
                vo.getEditPermission().getId()
        };
    }

    @Override
    public <R extends Record> SelectJoinStep<R> joinTables(SelectJoinStep<R> select,
            ConditionSortLimit conditions) {
        return select = select.leftJoin(userTable.getTableAsAlias()).on(userTable.getAlias("id").eq(table.getAlias("userId")));
    }

    @Override
    public RowMapper<WatchListVO> getRowMapper() {
        return rowMapper;
    }

    private final WatchListRowMapper rowMapper = new WatchListRowMapper();

    class WatchListRowMapper implements RowMapper<WatchListVO> {
        @Override
        public WatchListVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            int i = 0;
            WatchListVO wl = new WatchListVO();
            wl.setId(rs.getInt(++i));
            wl.setXid(rs.getString(++i));
            wl.setName(rs.getString(++i));
            wl.setUserId(rs.getInt(++i));
            wl.setType(rs.getString(++i));
            //Read the data
            try{
                Clob c = rs.getClob(++i);
                if(c != null) {
                    WatchListDbDataModel data = getObjectReader(WatchListDbDataModel.class).readValue(c.getCharacterStream());
                    if (data != null) {
                        wl.setQuery(data.query);
                        wl.setParams(data.params);
                        wl.setData(data.data);
                    }
                }
            }catch(Exception e){
                LOG.error(e.getMessage(), e);
            }
            wl.setReadPermission(new MangoPermission(rs.getInt(++i)));
            wl.setEditPermission(new MangoPermission(rs.getInt(++i)));
            return wl;
        }
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
