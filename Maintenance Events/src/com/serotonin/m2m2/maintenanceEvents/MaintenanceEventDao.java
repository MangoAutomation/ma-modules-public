/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.jooq.Record;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.LazyInitSupplier;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.AbstractVoDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.maintenanceEvents.db.tables.MaintenanceEvents;
import com.serotonin.m2m2.maintenanceEvents.db.tables.records.MaintenanceEventsRecord;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

@Repository()
public class MaintenanceEventDao extends AbstractVoDao<MaintenanceEventVO, MaintenanceEventsRecord, MaintenanceEvents> {

    private final String SELECT_POINT_IDS = "SELECT dataPointId FROM maintenanceEventDataPoints WHERE maintenanceEventId=?";
    private final String SELECT_DATA_SOURCE_IDS = "SELECT dataSourceId FROM maintenanceEventDataSources WHERE maintenanceEventId=?";

    private final String SELECT_POINT_XIDS = "SELECT xid from dataPoints AS dp JOIN maintenanceEventDataPoints mep ON mep.dataPointId = dp.id WHERE mep.maintenanceEventId=?";
    private final String SELECT_DATA_SOURCE_XIDS = "SELECT xid from dataSources AS ds JOIN maintenanceEventDataSources med ON med.dataSourceId = ds.id WHERE med.maintenanceEventId=?";

    private final String SELECT_POINTS;
    private final String SELECT_DATA_SOURCES;

    private final DataPointDao dataPointDao;
    private final DataSourceDao dataSourceDao;
    private final PermissionService permissionService;

    private static final LazyInitSupplier<MaintenanceEventDao> springInstance = new LazyInitSupplier<>(() -> {
        Object o = Common.getRuntimeContext().getBean(MaintenanceEventDao.class);
        if(o == null)
            throw new ShouldNeverHappenException("DAO not initialized in Spring Runtime Context");
        return (MaintenanceEventDao)o;
    });

    @Autowired
    private MaintenanceEventDao(
            DataPointDao dataPointDao, DataSourceDao dataSourceDao,
            @Qualifier(MangoRuntimeContextConfiguration.DAO_OBJECT_MAPPER_NAME)ObjectMapper mapper,
            ApplicationEventPublisher publisher,
            PermissionService permissionService) {
        super(AuditEvent.TYPE_NAME,
                MaintenanceEvents.MAINTENANCE_EVENTS, new TranslatableMessage("header.maintenanceEvents"),
                mapper, publisher);
        this.dataPointDao = dataPointDao;
        this.dataSourceDao = dataSourceDao;
        this.permissionService = permissionService;
        SELECT_POINTS = dataPointDao.getJoinedSelectQuery().getSQL() + " JOIN maintenanceEventDataPoints mep ON mep.dataPointId = dp.id WHERE mep.maintenanceEventId=?";
        SELECT_DATA_SOURCES = dataSourceDao.getJoinedSelectQuery().getSQL() + " JOIN maintenanceEventDataSources med ON med.dataSourceId = ds.id WHERE med.maintenanceEventId=?";
    }

    /**
     * Get cached instance from Spring Context
     * @return
     */
    public static MaintenanceEventDao getInstance() {
        return springInstance.get();
    }

    @Override
    public boolean delete(MaintenanceEventVO vo) {
        if (vo != null) {
            return getTransactionTemplate().execute(status -> {
                ejt.update("delete from eventHandlersMapping where eventTypeName=? and eventTypeRef1=?", new Object[] {
                        MaintenanceEventType.TYPE_NAME, vo.getId()});
                return MaintenanceEventDao.super.delete(vo);
            });
        }else {
            return false;
        }
    }

    @Override
    public void savePreRelationalData(MaintenanceEventVO existing, MaintenanceEventVO vo) {
        MangoPermission togglePermission = permissionService.findOrCreate(vo.getTogglePermission());
        vo.setTogglePermission(togglePermission);
    }

    @Override
    public void saveRelationalData(MaintenanceEventVO existing, MaintenanceEventVO vo) {
        if(existing == null) {
            ejt.batchUpdate(INSERT_DATA_SOURCE_IDS, new InsertDataSources(vo));
            ejt.batchUpdate(INSERT_DATA_POINT_IDS, new InsertDataPoints(vo));
        }else {
            //Delete and insert
            ejt.update(DELETE_DATA_SOURCE_IDS, new Object[] {vo.getId()});
            ejt.batchUpdate(INSERT_DATA_SOURCE_IDS, new InsertDataSources(vo));
            ejt.update(DELETE_DATA_POINT_IDS, new Object[] {vo.getId()});
            ejt.batchUpdate(INSERT_DATA_POINT_IDS, new InsertDataPoints(vo));
            if(!existing.getTogglePermission().equals(vo.getTogglePermission())) {
                permissionService.deletePermissions(existing.getTogglePermission());
            }
        }
    }

    @Override
    public void loadRelationalData(MaintenanceEventVO vo) {
        vo.setDataPoints(queryForList(SELECT_POINT_IDS, new Object[] {vo.getId()}, Integer.class));
        vo.setDataSources(queryForList(SELECT_DATA_SOURCE_IDS, new Object[] {vo.getId()}, Integer.class));
        //Populate permissions
        vo.setTogglePermission(permissionService.get(vo.getTogglePermission().getId()));
    }

    @Override
    public void deletePostRelationalData(MaintenanceEventVO vo) {
        //Clean permissions
        permissionService.deletePermissions(vo.getTogglePermission());
    }

    /**
     * Get the points for a maintenance event
     * @param maintenanceEventId
     * @param callback
     */
    public void getPoints(int maintenanceEventId, final MappedRowCallback<DataPointVO> callback){
        RowMapper<DataPointVO> pointMapper = dataPointDao.getRowMapper();
        this.ejt.query(SELECT_POINTS, new Object[]{maintenanceEventId}, new RowCallbackHandler(){
            private int row = 0;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                DataPointVO vo = pointMapper.mapRow(rs, row);
                dataPointDao.loadRelationalData(vo);
                callback.row(vo, row);
                row++;
            }

        });
    }

    /**
     * Get data point xids for a maintenance event
     * @param maintenanceEventId
     * @param callback
     */
    public void getPointXids(int maintenanceEventId, final MappedRowCallback<String> callback){
        this.ejt.query(SELECT_POINT_XIDS, new Object[]{maintenanceEventId}, new RowCallbackHandler(){
            private int row = 0;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                callback.row(rs.getString(1), row);
                row++;
            }

        });
    }

    /**
     * Get the data sources for a maintenance event
     * @param maintenanceEventId
     * @param callback
     */
    public void getDataSources(int maintenanceEventId, final MappedRowCallback<DataSourceVO> callback) {
        RowMapper<DataSourceVO> mapper = dataSourceDao.getRowMapper();
        this.ejt.query(SELECT_DATA_SOURCES, new Object[]{maintenanceEventId}, new RowCallbackHandler(){
            private int row = 0;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                DataSourceVO vo = mapper.mapRow(rs, row);
                dataSourceDao.loadRelationalData(vo);
                callback.row(vo, row);
                row++;
            }

        });
    }

    /**
     * Get data source xids for a maintenance event
     * @param maintenanceEventId
     * @param callback
     */
    public void getSourceXids(int maintenanceEventId, final MappedRowCallback<String> callback){
        this.ejt.query(SELECT_DATA_SOURCE_XIDS, new Object[]{maintenanceEventId}, new RowCallbackHandler(){
            private int row = 0;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                callback.row(rs.getString(1), row);
                row++;
            }

        });
    }

    private static final String SELECT_BY_DATA_POINT = "SELECT maintenanceEventId FROM maintenanceEventDataPoints WHERE dataPointId=?";
    private static final String SELECT_BY_DATA_SOURCE = "SELECT maintenanceEventId FROM maintenanceEventDataSources WHERE dataSourceId=?";
    /**
     * Get all maintenance events that have this data point in their list OR the its data source in the list
     * @param dataPointId
     * @param callback
     */
    public void getForDataPoint(int dataPointId, MappedRowCallback<MaintenanceEventVO> callback) {
        DataPointVO vo = dataPointDao.get(dataPointId);
        if(vo == null)
            return;
        getForDataPoint(vo, callback);
    }
    /**
     * Get all maintenance events that have this data point in their list OR the its data source in the list
     * @param dataPointXid
     * @param callback
     */
    public void getForDataPoint(String dataPointXid, MappedRowCallback<MaintenanceEventVO> callback) {
        DataPointVO vo = dataPointDao.getByXid(dataPointXid);
        if(vo == null)
            return;
        getForDataPoint(vo, callback);
    }
    protected void getForDataPoint(DataPointVO vo, MappedRowCallback<MaintenanceEventVO> callback) {

        //Get the events that are listed for this point
        List<Integer> ids = queryForList(SELECT_BY_DATA_POINT, new Object[] {vo.getId()}, Integer.class);
        if(ids.size() == 0)
            return;
        Select<Record> query = this.getJoinedSelectQuery().where(getIdField().in(ids));
        List<Object> args = query.getBindValues();
        query(query.getSQL(), args.toArray(), getRowMapper(), callback);

        //Get the events that are listed for the point's data source
        ids = queryForList(SELECT_BY_DATA_SOURCE, new Object[] {vo.getDataSourceId()}, Integer.class);
        if(ids.size() == 0)
            return;
        query = this.getJoinedSelectQuery().where(getIdField().in(ids));
        args = query.getBindValues();
        query(query.getSQL(), args.toArray(), getRowMapper(), callback);
    }

    /**
     * Get all Maintenance events that have this data source in their list
     * @param dataSourceXid
     * @param callback
     */
    public void getForDataSource(String dataSourceXid, MappedRowCallback<MaintenanceEventVO> callback) {
        Integer id = dataPointDao.getIdByXid(dataSourceXid);
        if(id == null)
            return;
        getForDataSource(id, callback);
    }


    /**
     * Get all Maintenance events that have this data source in their list
     * @param dataSourceId
     * @param callback
     */
    public void getForDataSource(int dataSourceId, MappedRowCallback<MaintenanceEventVO> callback) {
        //Get the events that are listed for the point's data source
        List<Integer> ids = queryForList(SELECT_BY_DATA_SOURCE, new Object[] {dataSourceId}, Integer.class);
        if(ids.size() == 0)
            return;
        Select<Record> query = this.getJoinedSelectQuery().where(getIdField().in(ids));
        List<Object> args = query.getBindValues();
        query(query.getSQL(), args.toArray(), getRowMapper(), callback);
    }

    private static final String INSERT_DATA_SOURCE_IDS = "INSERT INTO maintenanceEventDataSources (maintenanceEventId, dataSourceId) VALUES (?,?)";
    private static final String DELETE_DATA_SOURCE_IDS = "DELETE FROM maintenanceEventDataSources WHERE maintenanceEventId=?";

    private static final String INSERT_DATA_POINT_IDS = "INSERT INTO maintenanceEventDataPoints (maintenanceEventId, dataPointId) VALUES (?,?)";
    private static final String DELETE_DATA_POINT_IDS = "DELETE FROM maintenanceEventDataPoints WHERE maintenanceEventId=?";

    private static class InsertDataSources implements BatchPreparedStatementSetter {
        MaintenanceEventVO vo;

        InsertDataSources(MaintenanceEventVO vo) {
            this.vo = vo;
        }

        @Override
        public int getBatchSize() {
            return vo.getDataSources().size();
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setInt(1, vo.getId());
            ps.setInt(2, vo.getDataSources().get(i));
        }
    }

    private static class InsertDataPoints implements BatchPreparedStatementSetter {
        MaintenanceEventVO vo;

        InsertDataPoints(MaintenanceEventVO vo) {
            this.vo = vo;
        }

        @Override
        public int getBatchSize() {
            return vo.getDataPoints().size();
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setInt(1, vo.getId());
            ps.setInt(2, vo.getDataPoints().get(i));
        }
    }

    @Override
    protected String getXidPrefix() {
        return MaintenanceEventVO.XID_PREFIX;
    }

    @Override
    protected Record voToObjectArray(MaintenanceEventVO me) {
        Record record = table.newRecord();
        record.set(table.xid, me.getXid());
        record.set(table.alias, me.getName());
        record.set(table.alarmLevel, me.getAlarmLevel().value());
        record.set(table.scheduleType, me.getScheduleType());
        record.set(table.disabled, boolToChar(me.isDisabled()));
        record.set(table.activeYear, me.getActiveYear());
        record.set(table.activeMonth, me.getActiveMonth());
        record.set(table.activeDay, me.getActiveDay());
        record.set(table.activeHour, me.getActiveHour());
        record.set(table.activeMinute, me.getActiveMinute());
        record.set(table.activeSecond, me.getActiveSecond());
        record.set(table.activeCron, me.getActiveCron());
        record.set(table.inactiveYear, me.getInactiveYear());
        record.set(table.inactiveMonth, me.getInactiveMonth());
        record.set(table.inactiveDay, me.getInactiveDay());
        record.set(table.inactiveHour, me.getInactiveHour());
        record.set(table.inactiveMinute, me.getInactiveMinute());
        record.set(table.inactiveSecond, me.getInactiveSecond());
        record.set(table.inactiveCron, me.getInactiveCron());
        record.set(table.timeoutPeriods, me.getTimeoutPeriods());
        record.set(table.timeoutPeriodType, me.getTimeoutPeriodType());
        record.set(table.togglePermissionId, me.getTogglePermission().getId());
        return record;
    }

    @Override
    public RowMapper<MaintenanceEventVO> getRowMapper() {
        return new MaintenanceEventRowMapper();
    }

    @Override
    public MaintenanceEventVO mapRecord(Record record) {
        return null;
    }

    class MaintenanceEventRowMapper implements RowMapper<MaintenanceEventVO> {
        @Override
        public MaintenanceEventVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            MaintenanceEventVO me = new MaintenanceEventVO();
            int i = 0;
            me.setId(rs.getInt(++i));
            me.setXid(rs.getString(++i));
            me.setName(rs.getString(++i));
            me.setAlarmLevel(AlarmLevels.fromValue(rs.getInt(++i)));
            me.setScheduleType(rs.getInt(++i));
            me.setDisabled(charToBool(rs.getString(++i)));
            me.setActiveYear(rs.getInt(++i));
            me.setActiveMonth(rs.getInt(++i));
            me.setActiveDay(rs.getInt(++i));
            me.setActiveHour(rs.getInt(++i));
            me.setActiveMinute(rs.getInt(++i));
            me.setActiveSecond(rs.getInt(++i));
            me.setActiveCron(rs.getString(++i));
            me.setInactiveYear(rs.getInt(++i));
            me.setInactiveMonth(rs.getInt(++i));
            me.setInactiveDay(rs.getInt(++i));
            me.setInactiveHour(rs.getInt(++i));
            me.setInactiveMinute(rs.getInt(++i));
            me.setInactiveSecond(rs.getInt(++i));
            me.setInactiveCron(rs.getString(++i));
            me.setTimeoutPeriods(rs.getInt(++i));
            me.setTimeoutPeriodType(rs.getInt(++i));
            me.setTogglePermission(new MangoPermission(rs.getInt(++i)));
            return me;
        }
    }
}
