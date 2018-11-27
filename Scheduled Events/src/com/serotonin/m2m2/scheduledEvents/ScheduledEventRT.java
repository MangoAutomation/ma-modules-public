/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import java.text.ParseException;
import java.util.Date;

import org.joda.time.DateTime;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.util.timeout.ModelTimeoutClient;
import com.serotonin.m2m2.util.timeout.ModelTimeoutTask;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.timer.OneTimeTrigger;
import com.serotonin.timer.TimerTask;
import com.serotonin.timer.TimerTrigger;

/**
 * @author Matthew Lohbihler
 *
 */
public class ScheduledEventRT implements ModelTimeoutClient<Boolean> {
    private final ScheduledEventVO vo;
    private ScheduledEventType eventType;
    private boolean eventActive;
    private TimerTask activeTask;
    private TimerTask inactiveTask;

    public ScheduledEventRT(ScheduledEventVO vo) {
        this.vo = vo;
    }

    public ScheduledEventVO getVo() {
        return vo;
    }

    private void raiseEvent(long time) {
        Common.eventManager.raiseEvent(eventType, time, vo.isReturnToNormal(), vo.getAlarmLevel(), getMessage(), null);
        eventActive = true;
    }

    private void returnToNormal(long time) {
        Common.eventManager.returnToNormal(eventType, time);
        eventActive = false;
    }

    public TranslatableMessage getMessage() {
        return new TranslatableMessage("event.schedule.active", vo.getDescription());
    }

    public boolean isEventActive() {
        return eventActive;
    }

    @Override
    synchronized public void scheduleTimeout(Boolean active, long fireTime) {
        if (active)
            raiseEvent(fireTime);
        else
            returnToNormal(fireTime);
    }

    //
    //
    // /
    // / Lifecycle interface
    // /
    //
    //
    /**
     * Since the events are not returning to normal at shutdown anymore
     * we will ensure that if they returned to normal while Mango was off
     * we will return them to normal at startup.
     */
    public void initialize() {
        eventType = new ScheduledEventType(vo.getId());
        if (!vo.isReturnToNormal())
            eventType.setDuplicateHandling(EventType.DuplicateHandling.ALLOW);

        // Schedule the active event.
        TimerTrigger activeTrigger = createTrigger(true);
        activeTask = new ModelTimeoutTask<Boolean>(activeTrigger, this, true);

        if (vo.isReturnToNormal()) {
            TimerTrigger inactiveTrigger = createTrigger(false);
            inactiveTask = new ModelTimeoutTask<Boolean>(inactiveTrigger, this, false);

            if (vo.getScheduleType() != ScheduledEventVO.TYPE_ONCE) {
                // Check if we are currently active.
                if (inactiveTrigger.getNextExecutionTime() >= activeTrigger.getNextExecutionTime())
                    returnToNormal(System.currentTimeMillis());
                else
                    raiseEvent(System.currentTimeMillis());
            }
        }
    }

    public void terminate() {
        if (activeTask != null)
            activeTask.cancel();
        if (inactiveTask != null)
            inactiveTask.cancel();
        //returnToNormal(System.currentTimeMillis());
    }

    public void joinTermination() {
        // no op
    }

    private static final String[] weekdays = { "", "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };

    public TimerTrigger createTrigger(boolean activeTrigger) {
        if (!activeTrigger && !vo.isReturnToNormal())
            return null;

        if (vo.getScheduleType() == ScheduledEventVO.TYPE_CRON) {
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

        if (vo.getScheduleType() == ScheduledEventVO.TYPE_ONCE) {
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
        if (vo.getScheduleType() == ScheduledEventVO.TYPE_HOURLY)
            expression.append("* * * ?");
        else {
            expression.append(hour).append(' ');
            if (vo.getScheduleType() == ScheduledEventVO.TYPE_DAILY)
                expression.append("* * ?");
            else if (vo.getScheduleType() == ScheduledEventVO.TYPE_WEEKLY)
                expression.append("? * ").append(weekdays[day]);
            else {
                if (day > 0)
                    expression.append(day);
                else if (day == -1)
                    expression.append('L');
                else
                    expression.append(-day).append('L');

                if (vo.getScheduleType() == ScheduledEventVO.TYPE_MONTHLY)
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
        return "Scheduled Event " + this.vo.getXid();
    }

    final String PREFIX = "SCHED_EVT_";
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
