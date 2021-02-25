/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infiniteautomation.mango.db.tables.DataPoints;
import com.infiniteautomation.mango.db.tables.DataSources;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.MangoRuntimeContextConfiguration;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.LazyInitSupplier;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.AbstractVoDao;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.maintenanceEvents.db.tables.MaintenanceEventDataPoints;
import com.serotonin.m2m2.maintenanceEvents.db.tables.MaintenanceEventDataSources;
import com.serotonin.m2m2.maintenanceEvents.db.tables.MaintenanceEvents;
import com.serotonin.m2m2.maintenanceEvents.db.tables.records.MaintenanceEventsRecord;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

@Repository()
public class MaintenanceEventDao extends AbstractVoDao<MaintenanceEventVO, MaintenanceEventsRecord, MaintenanceEvents> {

    private final DataPointDao dataPointDao;
    private final DataSourceDao dataSourceDao;
    private final PermissionService permissionService;

    private static final LazyInitSupplier<MaintenanceEventDao> springInstance = new LazyInitSupplier<>(() -> {
        Object o = Common.getRuntimeContext().getBean(MaintenanceEventDao.class);
        if(o == null)
            throw new ShouldNeverHappenException("DAO not initialized in Spring Runtime Context");
        return (MaintenanceEventDao)o;
    });

    private final DataPoints dataPoints;
    private final DataSources dataSources;
    private final MaintenanceEventDataPoints mePoints;
    private final MaintenanceEventDataSources meSources;

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

        this.dataPoints = DataPoints.DATA_POINTS;
        this.dataSources = DataSources.DATA_SOURCES;
        this.mePoints = MaintenanceEventDataPoints.MAINTENANCE_EVENT_DATA_POINTS;
        this.meSources = MaintenanceEventDataSources.MAINTENANCE_EVENT_DATA_SOURCES;
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
        vo.setDataPoints(queryForList("SELECT dataPointId FROM maintenanceEventDataPoints WHERE maintenanceEventId=?", new Object[] {vo.getId()}, Integer.class));
        vo.setDataSources(queryForList("SELECT dataSourceId FROM maintenanceEventDataSources WHERE maintenanceEventId=?", new Object[] {vo.getId()}, Integer.class));
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
    public void getPoints(int maintenanceEventId, final Consumer<DataPointVO> callback){
        dataPointDao.getJoinedSelectQuery()
                .join(mePoints).on(mePoints.dataPointId.equal(dataPoints.id))
                .where(mePoints.maintenanceEventId.equal(maintenanceEventId)).forEach(record -> {
            DataPointVO vo = dataPointDao.mapRecord(record);
            dataPointDao.loadRelationalData(vo);
            callback.accept(vo);
        });
    }

    /**
     * Get data point xids for a maintenance event
     * @param maintenanceEventId
     * @param callback
     */
    public void getPointXids(int maintenanceEventId, final Consumer<String> callback){
        this.ejt.query("SELECT xid from dataPoints AS dp JOIN maintenanceEventDataPoints mep ON mep.dataPointId = dp.id WHERE mep.maintenanceEventId=?", new Object[]{maintenanceEventId}, new RowCallbackHandler(){
            private int row = 0;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                callback.accept(rs.getString(1));
                row++;
            }

        });
    }

    /**
     * Get the data sources for a maintenance event
     * @param maintenanceEventId
     * @param callback
     */
    public void getDataSources(int maintenanceEventId, final Consumer<DataSourceVO> callback) {
        dataSourceDao.getJoinedSelectQuery()
                .join(meSources).on(meSources.dataSourceId.equal(dataSources.id))
                .where(meSources.maintenanceEventId.equal(maintenanceEventId)).forEach(record -> {
            DataSourceVO vo = dataSourceDao.mapRecord(record);
            dataSourceDao.loadRelationalData(vo);
            callback.accept(vo);
        });
    }

    /**
     * Get data source xids for a maintenance event
     * @param maintenanceEventId
     * @param callback
     */
    public void getSourceXids(int maintenanceEventId, final Consumer<String> callback){
        this.ejt.query("SELECT xid from dataSources AS ds JOIN maintenanceEventDataSources med ON med.dataSourceId = ds.id WHERE med.maintenanceEventId=?", new Object[]{maintenanceEventId}, new RowCallbackHandler(){
            private int row = 0;

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                callback.accept(rs.getString(1));
                row++;
            }

        });
    }

    /**
     * Get all maintenance events that have this data point in their list OR the its data source in the list
     * @param dataPointId
     * @param callback
     */
    public void getForDataPoint(int dataPointId, Consumer<MaintenanceEventVO> callback) {
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
    public void getForDataPoint(String dataPointXid, Consumer<MaintenanceEventVO> callback) {
        DataPointVO vo = dataPointDao.getByXid(dataPointXid);
        if(vo == null)
            return;
        getForDataPoint(vo, callback);
    }
    protected void getForDataPoint(DataPointVO vo, Consumer<MaintenanceEventVO> callback) {
        Select<Record1<Integer>> idsForPoint = DSL.select(mePoints.maintenanceEventId)
                .from(mePoints)
                .where(mePoints.dataPointId.eq(vo.getId()));

        Select<Record1<Integer>> idsForSource = DSL.select(meSources.maintenanceEventId)
                .from(meSources)
                .where(meSources.dataSourceId.eq(vo.getDataSourceId()));

        getJoinedSelectQuery()
                .where(DSL.or(table.id.in(idsForPoint), table.id.in(idsForSource)))
                .forEach(record -> callback.accept(mapRecord(record)));
    }

    /**
     * Get all Maintenance events that have this data source in their list
     * @param dataSourceXid
     * @param callback
     */
    public void getForDataSource(String dataSourceXid, Consumer<MaintenanceEventVO> callback) {
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
    public void getForDataSource(int dataSourceId, Consumer<MaintenanceEventVO> callback) {
        Select<Record1<Integer>> idsForSource = DSL.select(meSources.maintenanceEventId)
                .from(meSources)
                .where(meSources.dataSourceId.eq(dataSourceId));

        getJoinedSelectQuery()
                .where(table.id.in(idsForSource))
                .forEach(record -> callback.accept(mapRecord(record)));
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
    protected Record toRecord(MaintenanceEventVO me) {
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
    public MaintenanceEventVO mapRecord(Record record) {
        MaintenanceEventVO me = new MaintenanceEventVO();
        me.setId(record.get(table.id));
        me.setXid(record.get(table.xid));
        me.setName(record.get(table.alias));
        me.setAlarmLevel(AlarmLevels.fromValue(record.get(table.alarmLevel)));
        me.setScheduleType(record.get(table.scheduleType));
        me.setDisabled(charToBool(record.get(table.disabled)));
        me.setActiveYear(record.get(table.activeYear));
        me.setActiveMonth(record.get(table.activeMonth));
        me.setActiveDay(record.get(table.activeDay));
        me.setActiveHour(record.get(table.activeHour));
        me.setActiveMinute(record.get(table.activeMinute));
        me.setActiveSecond(record.get(table.activeSecond));
        me.setActiveCron(record.get(table.activeCron));
        me.setInactiveYear(record.get(table.inactiveYear));
        me.setInactiveMonth(record.get(table.inactiveMonth));
        me.setInactiveDay(record.get(table.inactiveDay));
        me.setInactiveHour(record.get(table.inactiveHour));
        me.setInactiveMinute(record.get(table.inactiveMinute));
        me.setInactiveSecond(record.get(table.inactiveSecond));
        me.setInactiveCron(record.get(table.inactiveCron));
        me.setTimeoutPeriods(record.get(table.timeoutPeriods));
        me.setTimeoutPeriodType(record.get(table.timeoutPeriodType));
        me.setTogglePermission(new MangoPermission(record.get(table.togglePermissionId)));
        return me;
    }

}
