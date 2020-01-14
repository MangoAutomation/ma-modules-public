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

import org.jooq.Record;
import org.jooq.SelectJoinStep;
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
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.infiniteautomation.mango.spring.db.UserTableDefinition;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.LazyInitializer;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.AbstractDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.RoleDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.DataPointVO;
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

    @Autowired
    private WatchListDao(WatchListTableDefinition table, DataPointDao dataPointDao,
            UserTableDefinition userTable,
            @Qualifier(MangoRuntimeContextConfiguration.DAO_OBJECT_MAPPER_NAME)ObjectMapper mapper,
            ApplicationEventPublisher publisher) {
        super(AuditEvent.TYPE_NAME, table,
                new TranslatableMessage("internal.monitor.WATCHLIST_COUNT"),
                mapper, publisher);
        this.dataPointDao = dataPointDao;
        this.userTable = userTable;
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
                callback.accept(pointMapper.mapRow(rs, row));
                row++;
            }

        });
    }

    @Override
    public void saveRelationalData(WatchListVO vo, boolean insert) {
        if(!insert) {
            ejt.update("DELETE FROM watchListPoints WHERE watchListId=?", new Object[] { vo.getId() });
        }
        ejt.batchUpdate("INSERT INTO watchListPoints VALUES (?,?,?)", new InsertPoints(vo));

        //Replace the role mappings
        RoleDao.getInstance().replaceRolesOnVoPermission(vo.getReadRoles(), vo, PermissionService.READ, insert);
        RoleDao.getInstance().replaceRolesOnVoPermission(vo.getEditRoles(), vo, PermissionService.EDIT, insert);
    }

    @Override
    public void deleteRelationalData(WatchListVO vo) {
        RoleDao.getInstance().deleteRolesForVoPermission(vo, PermissionService.READ);
        RoleDao.getInstance().deleteRolesForVoPermission(vo, PermissionService.EDIT);
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
                jsonData
        };
    }

    @Override
    public <R extends Record> SelectJoinStep<R> joinTables(SelectJoinStep<R> select) {
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

    @Override
    protected void publishEvent(DaoEvent<WatchListVO> event) {
        // do nothing, want to publish events ourself after points added/removed
    }
}
