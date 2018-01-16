/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.infiniteautomation.mango.monitor.AtomicIntegerMonitor;
import com.infiniteautomation.mango.monitor.ValueMonitorOwner;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.BaseDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.type.AuditEventType;

/**
 * @author Matthew Lohbihler
 * 
 */
public class ScheduledEventDao extends BaseDao implements ValueMonitorOwner{
	
	//If you change this the Internal Metrics DS Should be updated
	public static final String COUNT_MONITOR_ID = "com.serotonin.m2m2.scheduledEvents.ScheduledEventDao.COUNT";
	public static final ScheduledEventDao instance = new ScheduledEventDao();
	
    //Monitor for count of table
    protected final AtomicIntegerMonitor countMonitor;
    
	private ScheduledEventDao(){
		this.countMonitor = new AtomicIntegerMonitor(COUNT_MONITOR_ID, new TranslatableMessage("internal.monitor.SCHEDULED_EVENT_COUNT"), this);
        this.countMonitor.setValue(this.count());
    	Common.MONITORED_VALUES.addIfMissingStatMonitor(this.countMonitor);
	};
	
    private static final String SCHEDULED_EVENT_SELECT = "select id, xid, alias, alarmLevel, scheduleType, "
            + "  returnToNormal, disabled, activeYear, activeMonth, activeDay, activeHour, activeMinute, activeSecond, "
            + "  activeCron, inactiveYear, inactiveMonth, inactiveDay, inactiveHour, inactiveMinute, inactiveSecond, "
            + "inactiveCron from scheduledEvents ";

    private static final String SCHEDULED_EVENT_COUNT = "SELECT COUNT(DISTINCT id) FROM scheduledEvents";
    
    public int count(){
    	return ejt.queryForInt(SCHEDULED_EVENT_COUNT, new Object[0], 0);
    }
    
    public String generateUniqueXid() {
        return generateUniqueXid(ScheduledEventVO.XID_PREFIX, "scheduledEvents");
    }

    public boolean isXidUnique(String xid, int excludeId) {
        return isXidUnique(xid, excludeId, "scheduledEvents");
    }

    public List<ScheduledEventVO> getScheduledEvents() {
        return query(SCHEDULED_EVENT_SELECT + " order by scheduleType", new ScheduledEventRowMapper());
    }

    public ScheduledEventVO getScheduledEvent(int id) {
        ScheduledEventVO se = queryForObject(SCHEDULED_EVENT_SELECT + "where id=?", new Object[] { id },
                new ScheduledEventRowMapper(), null);
        return se;
    }

    public ScheduledEventVO getScheduledEvent(String xid) {
        return queryForObject(SCHEDULED_EVENT_SELECT + "where xid=?", new Object[] { xid },
                new ScheduledEventRowMapper(), null);
    }

    class ScheduledEventRowMapper implements RowMapper<ScheduledEventVO> {
        @Override
        public ScheduledEventVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            ScheduledEventVO se = new ScheduledEventVO();
            int i = 0;
            se.setId(rs.getInt(++i));
            se.setXid(rs.getString(++i));
            se.setAlias(rs.getString(++i));
            se.setAlarmLevel(rs.getInt(++i));
            se.setScheduleType(rs.getInt(++i));
            se.setReturnToNormal(charToBool(rs.getString(++i)));
            se.setDisabled(charToBool(rs.getString(++i)));
            se.setActiveYear(rs.getInt(++i));
            se.setActiveMonth(rs.getInt(++i));
            se.setActiveDay(rs.getInt(++i));
            se.setActiveHour(rs.getInt(++i));
            se.setActiveMinute(rs.getInt(++i));
            se.setActiveSecond(rs.getInt(++i));
            se.setActiveCron(rs.getString(++i));
            se.setInactiveYear(rs.getInt(++i));
            se.setInactiveMonth(rs.getInt(++i));
            se.setInactiveDay(rs.getInt(++i));
            se.setInactiveHour(rs.getInt(++i));
            se.setInactiveMinute(rs.getInt(++i));
            se.setInactiveSecond(rs.getInt(++i));
            se.setInactiveCron(rs.getString(++i));
            return se;
        }
    }

    public void saveScheduledEvent(final ScheduledEventVO se) {
        if (se.getId() == Common.NEW_ID)
            insertScheduledEvent(se);
        else
            updateScheduledEvent(se);
    }

    private void insertScheduledEvent(ScheduledEventVO se) {
        se.setId(doInsert(
                "insert into scheduledEvents ("
                        + "  xid, alarmLevel, alias, scheduleType, returnToNormal, disabled, "
                        + "  activeYear, activeMonth, activeDay, activeHour, activeMinute, activeSecond, activeCron, "
                        + "  inactiveYear, inactiveMonth, inactiveDay, inactiveHour, inactiveMinute, inactiveSecond, inactiveCron "
                        + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new Object[] { se.getXid(), se.getAlarmLevel(), se.getAlias(), se.getScheduleType(),
                        boolToChar(se.isReturnToNormal()), boolToChar(se.isDisabled()), se.getActiveYear(),
                        se.getActiveMonth(), se.getActiveDay(), se.getActiveHour(), se.getActiveMinute(),
                        se.getActiveSecond(), se.getActiveCron(), se.getInactiveYear(), se.getInactiveMonth(),
                        se.getInactiveDay(), se.getInactiveHour(), se.getInactiveMinute(), se.getInactiveSecond(),
                        se.getInactiveCron() }));
        AuditEventType.raiseAddedEvent(AuditEvent.TYPE_NAME, se);
        this.countMonitor.increment();
    }

    private void updateScheduledEvent(ScheduledEventVO se) {
        ScheduledEventVO old = getScheduledEvent(se.getId());
        ejt.update(
                "update scheduledEvents set "
                        + "  xid=?, alarmLevel=?, alias=?, scheduleType=?, returnToNormal=?, disabled=?, "
                        + "  activeYear=?, activeMonth=?, activeDay=?, activeHour=?, activeMinute=?, activeSecond=?, activeCron=?, "
                        + "  inactiveYear=?, inactiveMonth=?, inactiveDay=?, inactiveHour=?, inactiveMinute=?, inactiveSecond=?, "
                        + "  inactiveCron=? " + "where id=?",
                new Object[] { se.getXid(), se.getAlarmLevel(), se.getAlias(), se.getScheduleType(),
                        boolToChar(se.isReturnToNormal()), boolToChar(se.isDisabled()), se.getActiveYear(),
                        se.getActiveMonth(), se.getActiveDay(), se.getActiveHour(), se.getActiveMinute(),
                        se.getActiveSecond(), se.getActiveCron(), se.getInactiveYear(), se.getInactiveMonth(),
                        se.getInactiveDay(), se.getInactiveHour(), se.getInactiveMinute(), se.getInactiveSecond(),
                        se.getInactiveCron(), se.getId() });
        AuditEventType.raiseChangedEvent(AuditEvent.TYPE_NAME, old, se);
    }

    public void deleteScheduledEvent(final int scheduledEventId) {
        ScheduledEventVO se = getScheduledEvent(scheduledEventId);
        final ExtendedJdbcTemplate ejt2 = ejt;
        if (se != null) {
            getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    ejt2.update("delete from eventHandlersMapping where eventTypeName=? and eventTypeRef1=?", new Object[] {
                            ScheduledEventType.TYPE_NAME, scheduledEventId });
                    ejt2.update("delete from scheduledEvents where id=?", new Object[] { scheduledEventId });
                }
            });

            AuditEventType.raiseDeletedEvent(AuditEvent.TYPE_NAME, se);
            this.countMonitor.decrement();
        }
    }
    
	/* (non-Javadoc)
	 * @see com.infiniteautomation.mango.monitor.ValueMonitorOwner#reset(java.lang.String)
	 */
	@Override
	public void reset(String monitorId) {
		//We only have one monitor so:
		this.countMonitor.setValue(this.count());
	}
}
