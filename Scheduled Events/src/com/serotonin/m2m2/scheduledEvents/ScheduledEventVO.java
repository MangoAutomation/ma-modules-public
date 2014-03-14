/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.util.ChangeComparable;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.web.taglib.Functions;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.validation.StringValidation;

/**
 * @author Matthew Lohbihler
 * 
 */
public class ScheduledEventVO implements ChangeComparable<ScheduledEventVO>, JsonSerializable {
    public static final String XID_PREFIX = "SE_";

    public static final int TYPE_HOURLY = 1;
    public static final int TYPE_DAILY = 2;
    public static final int TYPE_WEEKLY = 3;
    public static final int TYPE_MONTHLY = 4;
    public static final int TYPE_YEARLY = 5;
    public static final int TYPE_ONCE = 6;
    public static final int TYPE_CRON = 7;

    public static ExportCodes TYPE_CODES = new ExportCodes();
    static {
        TYPE_CODES.addElement(TYPE_HOURLY, "HOURLY", "scheduledEvents.type.hour");
        TYPE_CODES.addElement(TYPE_DAILY, "DAILY", "scheduledEvents.type.day");
        TYPE_CODES.addElement(TYPE_WEEKLY, "WEEKLY", "scheduledEvents.type.week");
        TYPE_CODES.addElement(TYPE_MONTHLY, "MONTHLY", "scheduledEvents.type.month");
        TYPE_CODES.addElement(TYPE_YEARLY, "YEARLY", "scheduledEvents.type.year");
        TYPE_CODES.addElement(TYPE_ONCE, "ONCE", "scheduledEvents.type.once");
        TYPE_CODES.addElement(TYPE_CRON, "CRON", "scheduledEvents.type.cron");
    }

    public boolean isNew() {
        return id == Common.NEW_ID;
    }

    private int id = Common.NEW_ID;
    private String xid;
    @JsonProperty
    private String alias;
    private int alarmLevel = AlarmLevels.NONE;
    private int scheduleType = TYPE_DAILY;
    @JsonProperty
    private boolean returnToNormal = true;
    @JsonProperty
    private boolean disabled = false;
    @JsonProperty
    private int activeYear;
    @JsonProperty
    private int activeMonth;
    @JsonProperty
    private int activeDay;
    @JsonProperty
    private int activeHour;
    @JsonProperty
    private int activeMinute;
    @JsonProperty
    private int activeSecond;
    @JsonProperty
    private String activeCron;
    @JsonProperty
    private int inactiveYear;
    @JsonProperty
    private int inactiveMonth;
    @JsonProperty
    private int inactiveDay;
    @JsonProperty
    private int inactiveHour;
    @JsonProperty
    private int inactiveMinute;
    @JsonProperty
    private int inactiveSecond;
    @JsonProperty
    private String inactiveCron;

    public EventTypeVO getEventType() {
        return new EventTypeVO(ScheduledEventType.TYPE_NAME, null, id, 0, getDescription(), alarmLevel);
    }

    public ScheduledEventRT createRuntime() {
        return new ScheduledEventRT(this);
    }

    public TranslatableMessage getDescription() {
        TranslatableMessage message;

        if (!StringUtils.isBlank(alias))
            message = new TranslatableMessage("common.default", alias);
        else if (scheduleType == TYPE_ONCE) {
            if (returnToNormal)
                message = new TranslatableMessage("event.schedule.onceUntil", Functions.getTime(new DateTime(
                        activeYear, activeMonth, activeDay, activeHour, activeMinute, activeSecond, 0).getMillis()),
                        Functions.getTime(new DateTime(inactiveYear, inactiveMonth, inactiveDay, inactiveHour,
                                inactiveMinute, inactiveSecond, 0).getMillis()));
            else
                message = new TranslatableMessage("event.schedule.onceAt", Functions.getTime(new DateTime(activeYear,
                        activeMonth, activeDay, activeHour, activeMinute, activeSecond, 0).getMillis()));
        }
        else if (scheduleType == TYPE_HOURLY) {
            String activeTime = StringUtils.leftPad(Integer.toString(activeMinute), 2, '0') + ":"
                    + StringUtils.leftPad(Integer.toString(activeSecond), 2, '0');
            if (returnToNormal)
                message = new TranslatableMessage("event.schedule.hoursUntil", activeTime, StringUtils.leftPad(
                        Integer.toString(inactiveMinute), 2, '0')
                        + ":" + StringUtils.leftPad(Integer.toString(inactiveSecond), 2, '0'));
            else
                message = new TranslatableMessage("event.schedule.hoursAt", activeTime);
        }
        else if (scheduleType == TYPE_DAILY) {
            if (returnToNormal)
                message = new TranslatableMessage("event.schedule.dailyUntil", activeTime(), inactiveTime());
            else
                message = new TranslatableMessage("event.schedule.dailyAt", activeTime());
        }
        else if (scheduleType == TYPE_WEEKLY) {
            if (returnToNormal)
                message = new TranslatableMessage("event.schedule.weeklyUntil", weekday(true), activeTime(),
                        weekday(false), inactiveTime());
            else
                message = new TranslatableMessage("event.schedule.weeklyAt", weekday(true), activeTime());
        }
        else if (scheduleType == TYPE_MONTHLY) {
            if (returnToNormal)
                message = new TranslatableMessage("event.schedule.monthlyUntil", monthday(true), activeTime(),
                        monthday(false), inactiveTime());
            else
                message = new TranslatableMessage("event.schedule.monthlyAt", monthday(true), activeTime());
        }
        else if (scheduleType == TYPE_YEARLY) {
            if (returnToNormal)
                message = new TranslatableMessage("event.schedule.yearlyUntil", monthday(true), month(true),
                        activeTime(), monthday(false), month(false), inactiveTime());
            else
                message = new TranslatableMessage("event.schedule.yearlyAt", monthday(true), month(true), activeTime());
        }
        else if (scheduleType == TYPE_CRON) {
            if (returnToNormal)
                message = new TranslatableMessage("event.schedule.cronUntil", activeCron, inactiveCron);
            else
                message = new TranslatableMessage("event.schedule.cronAt", activeCron);
        }
        else
            throw new ShouldNeverHappenException("Unknown schedule type: " + scheduleType);

        return message;
    }

    private TranslatableMessage getTypeMessage() {
        switch (scheduleType) {
        case TYPE_HOURLY:
            return new TranslatableMessage("scheduledEvents.type.hour");
        case TYPE_DAILY:
            return new TranslatableMessage("scheduledEvents.type.day");
        case TYPE_WEEKLY:
            return new TranslatableMessage("scheduledEvents.type.week");
        case TYPE_MONTHLY:
            return new TranslatableMessage("scheduledEvents.type.month");
        case TYPE_YEARLY:
            return new TranslatableMessage("scheduledEvents.type.year");
        case TYPE_ONCE:
            return new TranslatableMessage("scheduledEvents.type.once");
        case TYPE_CRON:
            return new TranslatableMessage("scheduledEvents.type.cron");
        }
        return null;
    }

    private String activeTime() {
        return StringUtils.leftPad(Integer.toString(activeHour), 2, '0') + ":"
                + StringUtils.leftPad(Integer.toString(activeMinute), 2, '0') + ":"
                + StringUtils.leftPad(Integer.toString(activeSecond), 2, '0');
    }

    private String inactiveTime() {
        return StringUtils.leftPad(Integer.toString(inactiveHour), 2, '0') + ":"
                + StringUtils.leftPad(Integer.toString(inactiveMinute), 2, '0') + ":"
                + StringUtils.leftPad(Integer.toString(inactiveSecond), 2, '0');
    }

    private static final String[] weekdays = { "", "common.day.mon", "common.day.tue", "common.day.wed",
            "common.day.thu", "common.day.fri", "common.day.sat", "common.day.sun" };

    private TranslatableMessage weekday(boolean active) {
        int day = activeDay;
        if (!active)
            day = inactiveDay;
        return new TranslatableMessage(weekdays[day]);
    }

    private TranslatableMessage monthday(boolean active) {
        int day = activeDay;

        if (!active)
            day = inactiveDay;

        if (day == -3)
            return new TranslatableMessage("common.day.thirdLast");
        if (day == -2)
            return new TranslatableMessage("common.day.secondLastLast");
        if (day == -1)
            return new TranslatableMessage("common.day.last");
        if (day != 11 && day % 10 == 1)
            return new TranslatableMessage("common.counting.st", Integer.toString(day));
        if (day != 12 && day % 10 == 2)
            return new TranslatableMessage("common.counting.nd", Integer.toString(day));
        if (day != 13 && day % 10 == 3)
            return new TranslatableMessage("common.counting.rd", Integer.toString(day));
        return new TranslatableMessage("common.counting.th", Integer.toString(day));
    }

    private static final String[] months = { "", "common.month.jan", "common.month.feb", "common.month.mar",
            "common.month.apr", "common.month.may", "common.month.jun", "common.month.jul", "common.month.aug",
            "common.month.sep", "common.month.oct", "common.month.nov", "common.month.dec" };

    private TranslatableMessage month(boolean active) {
        int day = activeDay;
        if (!active)
            day = inactiveDay;
        return new TranslatableMessage(months[day]);
    }

    @Override
    public String getTypeKey() {
        return "event.audit.scheduledEvent";
    }

    public void validate(ProcessResult response) {
        if (StringValidation.isLengthGreaterThan(alias, 50))
            response.addContextualMessage("alias", "scheduledEvents.validate.aliasTooLong");

        // Check that cron patterns are ok.
        if (scheduleType == TYPE_CRON) {
            try {
                new CronTimerTrigger(activeCron);
            }
            catch (Exception e) {
                response.addContextualMessage("activeCron", "scheduledEvents.validate.activeCron", e.getMessage());
            }

            if (returnToNormal) {
                try {
                    new CronTimerTrigger(inactiveCron);
                }
                catch (Exception e) {
                    response.addContextualMessage("inactiveCron", "scheduledEvents.validate.inactiveCron",
                            e.getMessage());
                }
            }
        }

        // Test that the triggers can be created.
        ScheduledEventRT rt = createRuntime();
        try {
            rt.createTrigger(true);
        }
        catch (RuntimeException e) {
            response.addContextualMessage("activeCron", "scheduledEvents.validate.activeTrigger", e.getMessage());
        }

        if (returnToNormal) {
            try {
                rt.createTrigger(false);
            }
            catch (RuntimeException e) {
                response.addContextualMessage("inactiveCron", "scheduledEvents.validate.inactiveTrigger",
                        e.getMessage());
            }
        }

        // If the event is once, make sure the active time is earlier than the inactive time.
        if (scheduleType == TYPE_ONCE && returnToNormal) {
            DateTime adt = new DateTime(activeYear, activeMonth, activeDay, activeHour, activeMinute, activeSecond, 0);
            DateTime idt = new DateTime(inactiveYear, inactiveMonth, inactiveDay, inactiveHour, inactiveMinute,
                    inactiveSecond, 0);
            if (idt.getMillis() <= adt.getMillis())
                response.addContextualMessage("scheduleType", "scheduledEvents.validate.invalidRtn");
        }
    }

    @Override
    public void addProperties(List<TranslatableMessage> list) {
        AuditEventType.addPropertyMessage(list, "common.xid", xid);
        AuditEventType.addPropertyMessage(list, "scheduledEvents.alias", alias);
        AuditEventType.addPropertyMessage(list, "common.alarmLevel", AlarmLevels.getAlarmLevelMessage(alarmLevel));
        AuditEventType.addPropertyMessage(list, "scheduledEvents.type", getTypeMessage());
        AuditEventType.addPropertyMessage(list, "common.rtn", returnToNormal);
        AuditEventType.addPropertyMessage(list, "common.disabled", disabled);
        AuditEventType.addPropertyMessage(list, "common.configuration", getDescription());
    }

    @Override
    public void addPropertyChanges(List<TranslatableMessage> list, ScheduledEventVO from) {
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid", from.xid, xid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "scheduledEvents.alias", from.alias, alias);
        AuditEventType.maybeAddAlarmLevelChangeMessage(list, "common.alarmLevel", from.alarmLevel, alarmLevel);
        if (from.scheduleType != scheduleType)
            AuditEventType.addPropertyChangeMessage(list, "scheduledEvents.type", from.getTypeMessage(),
                    getTypeMessage());
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.rtn", from.returnToNormal, returnToNormal);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.disabled", from.disabled, disabled);
        if (from.activeYear != activeYear || from.activeMonth != activeMonth || from.activeDay != activeDay
                || from.activeHour != activeHour || from.activeMinute != activeMinute
                || from.activeSecond != activeSecond || from.activeCron != activeCron
                || from.inactiveYear != inactiveYear || from.inactiveMonth != inactiveMonth
                || from.inactiveDay != inactiveDay || from.inactiveHour != inactiveHour
                || from.inactiveMinute != inactiveMinute || from.inactiveSecond != inactiveSecond
                || from.inactiveCron != inactiveCron)
            AuditEventType.maybeAddPropertyChangeMessage(list, "common.configuration", from.getDescription(),
                    getDescription());
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getActiveDay() {
        return activeDay;
    }

    public void setActiveDay(int activeDay) {
        this.activeDay = activeDay;
    }

    public int getActiveHour() {
        return activeHour;
    }

    public void setActiveHour(int activeHour) {
        this.activeHour = activeHour;
    }

    public int getActiveMinute() {
        return activeMinute;
    }

    public void setActiveMinute(int activeMinute) {
        this.activeMinute = activeMinute;
    }

    public int getActiveMonth() {
        return activeMonth;
    }

    public void setActiveMonth(int activeMonth) {
        this.activeMonth = activeMonth;
    }

    public int getActiveSecond() {
        return activeSecond;
    }

    public void setActiveSecond(int activeSecond) {
        this.activeSecond = activeSecond;
    }

    public int getActiveYear() {
        return activeYear;
    }

    public void setActiveYear(int activeYear) {
        this.activeYear = activeYear;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public int getInactiveDay() {
        return inactiveDay;
    }

    public void setInactiveDay(int inactiveDay) {
        this.inactiveDay = inactiveDay;
    }

    public int getInactiveHour() {
        return inactiveHour;
    }

    public void setInactiveHour(int inactiveHour) {
        this.inactiveHour = inactiveHour;
    }

    public int getInactiveMinute() {
        return inactiveMinute;
    }

    public void setInactiveMinute(int inactiveMinute) {
        this.inactiveMinute = inactiveMinute;
    }

    public int getInactiveMonth() {
        return inactiveMonth;
    }

    public void setInactiveMonth(int inactiveMonth) {
        this.inactiveMonth = inactiveMonth;
    }

    public int getInactiveSecond() {
        return inactiveSecond;
    }

    public void setInactiveSecond(int inactiveSecond) {
        this.inactiveSecond = inactiveSecond;
    }

    public int getInactiveYear() {
        return inactiveYear;
    }

    public void setInactiveYear(int inactiveYear) {
        this.inactiveYear = inactiveYear;
    }

    public boolean isReturnToNormal() {
        return returnToNormal;
    }

    public void setReturnToNormal(boolean returnToNormal) {
        this.returnToNormal = returnToNormal;
    }

    public int getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(int scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getActiveCron() {
        return activeCron;
    }

    public void setActiveCron(String activeCron) {
        this.activeCron = activeCron;
    }

    public String getInactiveCron() {
        return inactiveCron;
    }

    public void setInactiveCron(String inactiveCron) {
        this.inactiveCron = inactiveCron;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("xid", xid);
        writer.writeEntry("alarmLevel", AlarmLevels.CODES.getCode(alarmLevel));
        writer.writeEntry("scheduleType", TYPE_CODES.getCode(scheduleType));
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        String text = jsonObject.getString("alarmLevel");
        if (text != null) {
            alarmLevel = AlarmLevels.CODES.getId(text);
            if (!AlarmLevels.CODES.isValidId(alarmLevel))
                throw new TranslatableJsonException("emport.error.scheduledEvent.invalid", "alarmLevel", text,
                        AlarmLevels.CODES.getCodeList());
        }

        text = jsonObject.getString("scheduleType");
        if (text != null) {
            scheduleType = TYPE_CODES.getId(text);
            if (!TYPE_CODES.isValidId(scheduleType))
                throw new TranslatableJsonException("emport.error.scheduledEvent.invalid", "scheduleType", text,
                        TYPE_CODES.getCodeList());
        }
    }
}
