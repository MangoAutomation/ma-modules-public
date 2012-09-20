/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.rt.event.type.AuditEventType;

public class MaintenanceEventDao extends BaseDao {
    private static final String MAINTENANCE_EVENT_SELECT = //
    "select m.id, m.xid, m.dataSourceId, m.alias, m.alarmLevel, "
            + "  m.scheduleType, m.disabled, m.activeYear, m.activeMonth, m.activeDay, m.activeHour, m.activeMinute, "
            + "  m.activeSecond, m.activeCron, m.inactiveYear, m.inactiveMonth, m.inactiveDay, m.inactiveHour, "
            + "  m.inactiveMinute, m.inactiveSecond, m.inactiveCron, d.dataSourceType, d.name, d.xid " //
            + "from maintenanceEvents m join dataSources d on m.dataSourceId=d.id ";

    public String generateUniqueXid() {
        return generateUniqueXid(MaintenanceEventVO.XID_PREFIX, "maintenanceEvents");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "maintenanceEvents");
    }

    public List<MaintenanceEventVO> getMaintenanceEvents() {
        return query(MAINTENANCE_EVENT_SELECT, new MaintenanceEventRowMapper());
    }

    public MaintenanceEventVO getMaintenanceEvent(int id) {
        MaintenanceEventVO me = queryForObject(MAINTENANCE_EVENT_SELECT + "where m.id=?", new Object[] { id },
                new MaintenanceEventRowMapper());
        return me;
    }

    public MaintenanceEventVO getMaintenanceEvent(String xid) {
        return queryForObject(MAINTENANCE_EVENT_SELECT + "where m.xid=?", new Object[] { xid },
                new MaintenanceEventRowMapper(), null);
    }

    class MaintenanceEventRowMapper implements RowMapper<MaintenanceEventVO> {
        @Override
        public MaintenanceEventVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            MaintenanceEventVO me = new MaintenanceEventVO();
            int i = 0;
            me.setId(rs.getInt(++i));
            me.setXid(rs.getString(++i));
            me.setDataSourceId(rs.getInt(++i));
            me.setAlias(rs.getString(++i));
            me.setAlarmLevel(rs.getInt(++i));
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
            me.setDataSourceTypeId(rs.getString(++i));
            me.setDataSourceName(rs.getString(++i));
            me.setDataSourceXid(rs.getString(++i));
            return me;
        }
    }

    public void saveMaintenanceEvent(final MaintenanceEventVO me) {
        if (me.getId() == Common.NEW_ID)
            insertMaintenanceEvent(me);
        else
            updateMaintenanceEvent(me);
    }

    private void insertMaintenanceEvent(MaintenanceEventVO me) {
        me.setId(doInsert(
                "insert into maintenanceEvents ("
                        + "  xid, dataSourceId, alias, alarmLevel, scheduleType, disabled, "
                        + "  activeYear, activeMonth, activeDay, activeHour, activeMinute, activeSecond, activeCron, "
                        + "  inactiveYear, inactiveMonth, inactiveDay, inactiveHour, inactiveMinute, inactiveSecond, inactiveCron "
                        + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[] { me.getXid(), me.getDataSourceId(), me.getAlias(), me.getAlarmLevel(),
                        me.getScheduleType(), boolToChar(me.isDisabled()), me.getActiveYear(), me.getActiveMonth(),
                        me.getActiveDay(), me.getActiveHour(), me.getActiveMinute(), me.getActiveSecond(),
                        me.getActiveCron(), me.getInactiveYear(), me.getInactiveMonth(), me.getInactiveDay(),
                        me.getInactiveHour(), me.getInactiveMinute(), me.getInactiveSecond(), me.getInactiveCron() }));
        AuditEventType.raiseAddedEvent(AuditEvent.TYPE_NAME, me);
    }

    private void updateMaintenanceEvent(MaintenanceEventVO me) {
        MaintenanceEventVO old = getMaintenanceEvent(me.getId());
        ejt.update(
                "update maintenanceEvents set "
                        + "  xid=?, dataSourceId=?, alias=?, alarmLevel=?, scheduleType=?, disabled=?, "
                        + "  activeYear=?, activeMonth=?, activeDay=?, activeHour=?, activeMinute=?, activeSecond=?, activeCron=?, "
                        + "  inactiveYear=?, inactiveMonth=?, inactiveDay=?, inactiveHour=?, inactiveMinute=?, inactiveSecond=?, "
                        + "  inactiveCron=? "//
                        + "where id=?",
                new Object[] { me.getXid(), me.getDataSourceId(), me.getAlias(), me.getAlarmLevel(),
                        me.getScheduleType(), boolToChar(me.isDisabled()), me.getActiveYear(), me.getActiveMonth(),
                        me.getActiveDay(), me.getActiveHour(), me.getActiveMinute(), me.getActiveSecond(),
                        me.getActiveCron(), me.getInactiveYear(), me.getInactiveMonth(), me.getInactiveDay(),
                        me.getInactiveHour(), me.getInactiveMinute(), me.getInactiveSecond(), me.getInactiveCron(),
                        me.getId() });
        AuditEventType.raiseChangedEvent(AuditEvent.TYPE_NAME, old, me);
    }

    public void deleteMaintenanceEventsForDataSource(int dataSourceId) {
        List<Integer> ids = queryForList("select id from maintenanceEvents where dataSourceId=?",
                new Object[] { dataSourceId }, Integer.class);
        for (Integer id : ids)
            deleteMaintenanceEvent(id);
    }

    public void deleteMaintenanceEvent(final int maintenanceEventId) {
        MaintenanceEventVO me = getMaintenanceEvent(maintenanceEventId);
        final ExtendedJdbcTemplate ejt2 = ejt;
        if (me != null) {
            getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    ejt2.update("delete from eventHandlers where eventTypeName=? and eventTypeRef1=?", new Object[] {
                            MaintenanceEventType.TYPE_NAME, maintenanceEventId });
                    ejt2.update("delete from maintenanceEvents where id=?", new Object[] { maintenanceEventId });
                }
            });

            AuditEventType.raiseDeletedEvent(AuditEvent.TYPE_NAME, me);
        }
    }
}
