/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.util.function.Consumer;
import java.util.List;

import org.jooq.BatchBindStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.infiniteautomation.mango.db.tables.DataPoints;
import com.infiniteautomation.mango.db.tables.DataSources;
import com.infiniteautomation.mango.db.tables.EventHandlersMapping;
import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.DaoDependencies;
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
    private final EventHandlersMapping eventHandlersMapping;

    @Autowired
    private MaintenanceEventDao(
            DataPointDao dataPointDao, DataSourceDao dataSourceDao,
            DaoDependencies dependencies) {
        super(dependencies, AuditEvent.TYPE_NAME,
                MaintenanceEvents.MAINTENANCE_EVENTS, new TranslatableMessage("header.maintenanceEvents"));
        this.dataPointDao = dataPointDao;
        this.dataSourceDao = dataSourceDao;

        this.dataPoints = DataPoints.DATA_POINTS;
        this.dataSources = DataSources.DATA_SOURCES;
        this.mePoints = MaintenanceEventDataPoints.MAINTENANCE_EVENT_DATA_POINTS;
        this.meSources = MaintenanceEventDataSources.MAINTENANCE_EVENT_DATA_SOURCES;
        this.eventHandlersMapping = EventHandlersMapping.EVENT_HANDLERS_MAPPING;
    }

    /**
     * Get cached instance from Spring Context
     * @return
     */
    public static MaintenanceEventDao getInstance() {
        return springInstance.get();
    }

    @Override
    public void deleteRelationalData(MaintenanceEventVO vo) {
        create.deleteFrom(eventHandlersMapping)
                .where(eventHandlersMapping.eventTypeName.eq(MaintenanceEventType.TYPE_NAME))
                .and(eventHandlersMapping.eventTypeRef1.eq(vo.getId()))
                .execute();
    }

    @Override
    public void savePreRelationalData(MaintenanceEventVO existing, MaintenanceEventVO vo) {
        MangoPermission togglePermission = permissionService.findOrCreate(vo.getTogglePermission());
        vo.setTogglePermission(togglePermission);
    }

    @Override
    public void saveRelationalData(MaintenanceEventVO existing, MaintenanceEventVO vo) {
        if (existing != null) {
            //Delete and insert
            create.deleteFrom(meSources).where(meSources.maintenanceEventId.equal(vo.getId())).execute();
            create.deleteFrom(mePoints).where(mePoints.maintenanceEventId.equal(vo.getId())).execute();
        }

        List<Integer> dataSources = vo.getDataSources();
        if (dataSources.size() > 0) {
            BatchBindStep batchSources = create.batch(
                    create.insertInto(meSources)
                            .columns(meSources.maintenanceEventId, meSources.dataSourceId)
                            .values((Integer) null, null)
            );
            for(Integer dataSourceId : dataSources) batchSources.bind(vo.getId(), dataSourceId);
            batchSources.execute();
        }

        List<Integer> dataPoints = vo.getDataPoints();
        if (dataPoints.size() > 0) {
            BatchBindStep batchPoints = create.batch(
                    DSL.insertInto(mePoints)
                            .columns(mePoints.maintenanceEventId, mePoints.dataPointId)
                            .values((Integer) null, null)
            );
            for(Integer dataPointId : dataPoints) batchPoints.bind(vo.getId(), dataPointId);
            batchPoints.execute();
        }

        if (existing != null && !existing.getTogglePermission().equals(vo.getTogglePermission())) {
            permissionService.deletePermissions(existing.getTogglePermission());
        }
    }

    @Override
    public void loadRelationalData(MaintenanceEventVO vo) {
        vo.setDataPoints(
            this.create.select().from(mePoints)
                .where(mePoints.maintenanceEventId.equal(vo.getId()))
                .fetch(mePoints.dataPointId, Integer.class)
        );
        vo.setDataSources(
            this.create.select().from(meSources)
                    .where(meSources.maintenanceEventId.equal(vo.getId()))
                    .fetch(meSources.dataSourceId, Integer.class)
        );
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
    public void getPointXids(int maintenanceEventId, final Consumer<String> callback) {
        dataPointDao.getJoinedSelectQuery()
                .join(mePoints).on(mePoints.dataPointId.equal(dataPoints.id))
                .where(mePoints.maintenanceEventId.equal(maintenanceEventId)).forEach(record -> {
            callback.accept(record.get(dataPointDao.getXidField()));
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
    public void getSourceXids(int maintenanceEventId, final Consumer<String> callback) {
        dataSourceDao.getJoinedSelectQuery()
                .join(meSources).on(meSources.dataSourceId.equal(dataSources.id))
                .where(meSources.maintenanceEventId.equal(maintenanceEventId)).forEach(record -> {
            callback.accept(record.get(dataSourceDao.getXidField()));
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
