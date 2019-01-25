/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Matthew Lohbihler
 *
 */
public class ScheduledEventsDwr extends ModuleDwr {
    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public List<ScheduledEventVO> getScheduledEvents() {
        return ScheduledEventDao.getInstance().getScheduledEvents();
    }

    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public ScheduledEventVO getScheduledEvent(int id) {

        if (id == Common.NEW_ID) {
            DateTime dt = new DateTime();
            ScheduledEventVO se = new ScheduledEventVO();
            se.setXid(ScheduledEventDao.getInstance().generateUniqueXid());
            se.setActiveYear(dt.getYear());
            se.setInactiveYear(dt.getYear());
            se.setActiveMonth(dt.getMonthOfYear());
            se.setInactiveMonth(dt.getMonthOfYear());
            return se;
        }
        return ScheduledEventDao.getInstance().getScheduledEvent(id);
    }

    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public ProcessResult saveScheduledEvent(int id, String xid, String alias, AlarmLevels alarmLevel, int scheduleType,
            boolean returnToNormal, boolean disabled, int activeYear, int activeMonth, int activeDay, int activeHour,
            int activeMinute, int activeSecond, String activeCron, int inactiveYear, int inactiveMonth,
            int inactiveDay, int inactiveHour, int inactiveMinute, int inactiveSecond, String inactiveCron) {

        // Validate the given information. If there is a problem, return an appropriate error message.
        ScheduledEventVO se = new ScheduledEventVO();
        se.setId(id);
        se.setXid(xid);
        se.setAlias(alias);
        se.setAlarmLevel(alarmLevel);
        se.setScheduleType(scheduleType);
        se.setReturnToNormal(returnToNormal);
        se.setDisabled(disabled);
        se.setActiveYear(activeYear);
        se.setActiveMonth(activeMonth);
        se.setActiveDay(activeDay);
        se.setActiveHour(activeHour);
        se.setActiveMinute(activeMinute);
        se.setActiveSecond(activeSecond);
        se.setActiveCron(activeCron);
        se.setInactiveYear(inactiveYear);
        se.setInactiveMonth(inactiveMonth);
        se.setInactiveDay(inactiveDay);
        se.setInactiveHour(inactiveHour);
        se.setInactiveMinute(inactiveMinute);
        se.setInactiveSecond(inactiveSecond);
        se.setInactiveCron(inactiveCron);

        ProcessResult response = new ProcessResult();
        ScheduledEventDao scheduledEventDao = ScheduledEventDao.getInstance();

        if (StringUtils.isBlank(xid))
            response.addContextualMessage("xid", "validate.required");
        else if (!scheduledEventDao.isXidUnique(xid, id))
            response.addContextualMessage("xid", "validate.xidUsed");

        se.validate(response);

        // Save the scheduled event
        if (!response.getHasMessages())
            RTMDefinition.instance.saveScheduledEvent(se);

        response.addData("seId", se.getId());
        return response;
    }

    @DwrPermission(custom = SystemSettingsDao.PERMISSION_DATASOURCE)
    public void deleteScheduledEvent(int seId) {
        RTMDefinition.instance.deleteScheduledEvent(seId);
    }
}
