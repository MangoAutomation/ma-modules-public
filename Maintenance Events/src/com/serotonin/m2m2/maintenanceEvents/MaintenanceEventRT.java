/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;

import com.infiniteautomation.mango.util.datetime.NextTimePeriodAdjuster;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.ReturnCause;
import com.serotonin.m2m2.util.timeout.ModelTimeoutClient;
import com.serotonin.m2m2.util.timeout.ModelTimeoutTask;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.timer.OneTimeTrigger;
import com.serotonin.timer.TimerTask;
import com.serotonin.timer.TimerTrigger;

public class MaintenanceEventRT implements ModelTimeoutClient<Boolean> {
    private final MaintenanceEventVO vo;
    private MaintenanceEventType eventType;
    private boolean eventActive;
    private TimerTask activeTask;
    private TimerTask inactiveTask;

    public MaintenanceEventRT(MaintenanceEventVO vo) {
        this.vo = vo;
    }

    public MaintenanceEventVO getVo() {
        return vo;
    }

    private void raiseEvent(long time) {
        if (!eventActive) {
            Common.eventManager.raiseEvent(eventType, time, true, vo.getAlarmLevel(), getMessage(), null);
            eventActive = true;
        }
    }

    private void returnToNormal(long time) {
        if (eventActive) {
            Common.eventManager.returnToNormal(eventType, time);
            eventActive = false;
        }
    }

    public TranslatableMessage getMessage() {
        return new TranslatableMessage("event.maintenance.active", vo.getDescription());
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public boolean toggle() {
        scheduleTimeout(!eventActive, System.currentTimeMillis());
        return eventActive;
    }

    @Override
    synchronized public void scheduleTimeout(Boolean active, long fireTime) {
        if (active) {
            raiseEvent(fireTime);
            //Set the timeout if it is a manual event
            if (vo.getScheduleType() == MaintenanceEventVO.TYPE_MANUAL) {
                //Do we have a timeout set?
                if(vo.getTimeoutPeriods() > 0) {
                    //Compute fire time
                    NextTimePeriodAdjuster adjuster = new NextTimePeriodAdjuster(vo.getTimeoutPeriodType(), vo.getTimeoutPeriods());
                    ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Common.timer.currentTimeMillis()), TimeZone.getDefault().toZoneId());
                    time = (ZonedDateTime) adjuster.adjustInto(time);
                    TimerTrigger inactiveTrigger = new OneTimeTrigger(new Date(time.toInstant().toEpochMilli()));
                    inactiveTask = new ModelTimeoutTask<Boolean>(inactiveTrigger, this, false);
                }
            }
        }else
            returnToNormal(fireTime);
    }

    //
    //
    // Lifecycle interface
    //
    public void initialize() {
        eventType = new MaintenanceEventType(vo);

        if (vo.getScheduleType() != MaintenanceEventVO.TYPE_MANUAL) {
            // Schedule the active event.
            TimerTrigger activeTrigger = createTrigger(true);
            activeTask = new ModelTimeoutTask<Boolean>(activeTrigger, this, true);

            // Schedule the inactive event
            TimerTrigger inactiveTrigger = createTrigger(false);
            inactiveTask = new ModelTimeoutTask<Boolean>(inactiveTrigger, this, false);

            if (vo.getScheduleType() != MaintenanceEventVO.TYPE_ONCE) {
                // Check if we are currently active.
                if (inactiveTrigger.getNextExecutionTime() < activeTrigger.getNextExecutionTime())
                    raiseEvent(System.currentTimeMillis());
            }
        }
    }

    public void terminate() {
        if (activeTask != null)
            activeTask.cancel();
        if (inactiveTask != null)
            inactiveTask.cancel();

        if (eventActive)
            Common.eventManager.returnToNormal(eventType, System.currentTimeMillis(),
                    ReturnCause.SOURCE_DISABLED);
    }

    public void joinTermination() {
        // no op
    }

    private static final String[] weekdays = { "", "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };

    public TimerTrigger createTrigger(boolean activeTrigger) {
        if (vo.getScheduleType() == MaintenanceEventVO.TYPE_MANUAL)
            return null;

        if (vo.getScheduleType() == MaintenanceEventVO.TYPE_CRON) {
            try {
                if (activeTrigger)
                    return new CronTimerTrigger(vo.getActiveCron());
                return new CronTimerTrigger(vo.getInactiveCron());
            }
            catch (ParseException e) {
                // Should never happen, so wrap and rethrow
                throw new ShouldNeverHappenException(e);
            }
        }

        if (vo.getScheduleType() == MaintenanceEventVO.TYPE_ONCE) {
            DateTime dt;
            if (activeTrigger)
                dt = new DateTime(vo.getActiveYear(), vo.getActiveMonth(), vo.getActiveDay(), vo.getActiveHour(),
                        vo.getActiveMinute(), vo.getActiveSecond(), 0);
            else
                dt = new DateTime(vo.getInactiveYear(), vo.getInactiveMonth(), vo.getInactiveDay(),
                        vo.getInactiveHour(), vo.getInactiveMinute(), vo.getInactiveSecond(), 0);
            return new OneTimeTrigger(new Date(dt.getMillis()));
        }

        int month = vo.getActiveMonth();
        int day = vo.getActiveDay();
        int hour = vo.getActiveHour();
        int minute = vo.getActiveMinute();
        int second = vo.getActiveSecond();
        if (!activeTrigger) {
            month = vo.getInactiveMonth();
            day = vo.getInactiveDay();
            hour = vo.getInactiveHour();
            minute = vo.getInactiveMinute();
            second = vo.getInactiveSecond();
        }

        StringBuilder expression = new StringBuilder();
        expression.append(second).append(' ');
        expression.append(minute).append(' ');
        if (vo.getScheduleType() == MaintenanceEventVO.TYPE_HOURLY)
            expression.append("* * * ?");
        else {
            expression.append(hour).append(' ');
            if (vo.getScheduleType() == MaintenanceEventVO.TYPE_DAILY)
                expression.append("* * ?");
            else if (vo.getScheduleType() == MaintenanceEventVO.TYPE_WEEKLY)
                expression.append("? * ").append(weekdays[day]);
            else {
                if (day > 0)
                    expression.append(day);
                else if (day == -1)
                    expression.append('L');
                else
                    expression.append(-day).append('L');

                if (vo.getScheduleType() == MaintenanceEventVO.TYPE_MONTHLY)
                    expression.append(" * ?");
                else
                    expression.append(' ').append(month).append(" ?");
            }
        }

        CronTimerTrigger cronTrigger;
        try {
            cronTrigger = new CronTimerTrigger(expression.toString());
        }
        catch (ParseException e) {
            // Should never happen, so wrap and rethrow
            throw new ShouldNeverHappenException(e);
        }
        return cronTrigger;
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.util.timeout.ModelTimeoutClient#getThreadName()
     */
    @Override
    public String getThreadName() {
        return "Maintenence Event " + this.vo.getXid();
    }

    final String PREFIX = "MAINT_";
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.util.timeout.ModelTimeoutClient#getTaskId()
     */
    @Override
    public String getTaskId() {
        return PREFIX + this.vo.getXid();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.util.timeout.ModelTimeoutClient#getQueueSize()
     */
    @Override
    public int getQueueSize() {
        return 0;
    }
}
