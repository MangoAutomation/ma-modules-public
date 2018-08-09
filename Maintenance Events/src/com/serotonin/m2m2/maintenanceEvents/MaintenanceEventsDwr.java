/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;

/**
 * @author Matthew Lohbihler
 */
public class MaintenanceEventsDwr extends ModuleDwr {
    @DwrPermission(admin = true)
    public ProcessResult getMaintenanceEvents() {
        ProcessResult response = new ProcessResult();
        final Translations translations = getTranslations();

        List<MaintenanceEventVO> events = MaintenanceEventDao.getInstance().getAllFull();
        Collections.sort(events, new Comparator<MaintenanceEventVO>() {
            @Override
            public int compare(MaintenanceEventVO m1, MaintenanceEventVO m2) {
                return m1.getDescription().translate(translations)
                        .compareTo(m1.getDescription().translate(translations));
            }
        });
        response.addData(MODEL_ATTR_EVENTS, events);

        List<IntStringPair> dataSources = new ArrayList<IntStringPair>();
        for (DataSourceVO<?> ds : DataSourceDao.getInstance().getDataSources())
            dataSources.add(new IntStringPair(ds.getId(), ds.getName()));
        response.addData("dataSources", dataSources);

        return response;
    }

    @DwrPermission(admin = true)
    public ProcessResult getMaintenanceEvent(int id) {
        ProcessResult response = new ProcessResult();

        MaintenanceEventVO me;
        boolean activated = false;
        if (id == Common.NEW_ID) {
            DateTime dt = new DateTime();
            me = new MaintenanceEventVO();
            me.setXid(MaintenanceEventDao.getInstance().generateUniqueXid());
            me.setActiveYear(dt.getYear());
            me.setInactiveYear(dt.getYear());
            me.setActiveMonth(dt.getMonthOfYear());
            me.setInactiveMonth(dt.getMonthOfYear());
        }
        else {
            me = MaintenanceEventDao.getInstance().getFull(id);

            MaintenanceEventRT rt = RTMDefinition.instance.getRunningMaintenanceEvent(me.getId());
            if (rt != null)
                activated = rt.isEventActive();
        }

        response.addData("me", me);
        response.addData("activated", activated);

        return response;
    }

    @DwrPermission(admin = true)
    public ProcessResult saveMaintenanceEvent(int id, String xid, int dataSourceId, String alias, int alarmLevel,
            int scheduleType, boolean disabled, int activeYear, int activeMonth, int activeDay, int activeHour,
            int activeMinute, int activeSecond, String activeCron, int inactiveYear, int inactiveMonth,
            int inactiveDay, int inactiveHour, int inactiveMinute, int inactiveSecond, String inactiveCron) {
        MaintenanceEventVO e = new MaintenanceEventVO();
        e.setId(id);
        e.setXid(xid);
        e.getDataSources().add(dataSourceId);
        e.setName(alias);
        e.setAlarmLevel(alarmLevel);
        e.setScheduleType(scheduleType);
        e.setDisabled(disabled);
        e.setActiveYear(activeYear);
        e.setActiveMonth(activeMonth);
        e.setActiveDay(activeDay);
        e.setActiveHour(activeHour);
        e.setActiveMinute(activeMinute);
        e.setActiveSecond(activeSecond);
        e.setActiveCron(activeCron);
        e.setInactiveYear(inactiveYear);
        e.setInactiveMonth(inactiveMonth);
        e.setInactiveDay(inactiveDay);
        e.setInactiveHour(inactiveHour);
        e.setInactiveMinute(inactiveMinute);
        e.setInactiveSecond(inactiveSecond);
        e.setInactiveCron(inactiveCron);

        ProcessResult response = new ProcessResult();
        e.validate(response);

        // Save the maintenance event
        if (!response.getHasMessages()) {
            RTMDefinition.instance.saveMaintenanceEvent(e);
            response.addData("meId", e.getId());
        }

        return response;
    }

    @DwrPermission(admin = true)
    public void deleteMaintenanceEvent(int meId) {
        RTMDefinition.instance.deleteMaintenanceEvent(meId);
    }

    @DwrPermission(admin = true)
    public ProcessResult toggleMaintenanceEvent(int id) {
        ProcessResult response = new ProcessResult();

        MaintenanceEventRT rt = RTMDefinition.instance.getRunningMaintenanceEvent(id);
        boolean activated = false;
        if (rt == null)
            response.addMessage(new TranslatableMessage("maintenanceEvents.toggle.disabled"));
        else
            activated = rt.toggle();

        response.addData("activated", activated);

        return response;
    }
}
