/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.util.Functions;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;

public class MaintenanceEventVO extends AbstractVO {

    private static final long serialVersionUID = 1L;

    public static final String XID_PREFIX = "ME_";

    public static final int TYPE_MANUAL = 1;
    public static final int TYPE_HOURLY = 2;
    public static final int TYPE_DAILY = 3;
    public static final int TYPE_WEEKLY = 4;
    public static final int TYPE_MONTHLY = 5;
    public static final int TYPE_YEARLY = 6;
    public static final int TYPE_ONCE = 7;
    public static final int TYPE_CRON = 8;

    public static ExportCodes TYPE_CODES = new ExportCodes();
    static {
        TYPE_CODES.addElement(TYPE_MANUAL, "MANUAL", "maintenanceEvents.type.manual");
        TYPE_CODES.addElement(TYPE_HOURLY, "HOURLY", "maintenanceEvents.type.hour");
        TYPE_CODES.addElement(TYPE_DAILY, "DAILY", "maintenanceEvents.type.day");
        TYPE_CODES.addElement(TYPE_WEEKLY, "WEEKLY", "maintenanceEvents.type.week");
        TYPE_CODES.addElement(TYPE_MONTHLY, "MONTHLY", "maintenanceEvents.type.month");
        TYPE_CODES.addElement(TYPE_YEARLY, "YEARLY", "maintenanceEvents.type.year");
        TYPE_CODES.addElement(TYPE_ONCE, "ONCE", "maintenanceEvents.type.once");
        TYPE_CODES.addElement(TYPE_CRON, "CRON", "maintenanceEvents.type.cron");
    }

    private List<Integer> dataSources = new ArrayList<>();
    private List<Integer> dataPoints = new ArrayList<>();
    private AlarmLevels alarmLevel = AlarmLevels.NONE;
    private int scheduleType = TYPE_MANUAL;
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
    //Manual events can have a timeout if timeout periods > 0
    private int timeoutPeriods = 0;
    private int timeoutPeriodType = TimePeriods.HOURS;
    @JsonProperty
    private MangoPermission togglePermission = new MangoPermission();

    @Override
    public boolean isNew() {
        return id == Common.NEW_ID;
    }

    @Override
    public String getXid() {
        return xid;
    }

    @Override
    public void setXid(String xid) {
        this.xid = xid;
    }

    public List<Integer> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<Integer> dataSourceIds) {
        this.dataSources = dataSourceIds;
    }

    public List<Integer> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<Integer> dataPointIds) {
        this.dataPoints = dataPointIds;
    }

    /**
     * Deprecated as we should just use the name. Leaving here as I believe these are probably accessed on the legacy page via DWR.
     * @return
     */
    @Deprecated
    public String getAlias() {
        return name;
    }

    @Deprecated
    public void setAlias(String alias) {
        this.name = alias;
    }

    public AlarmLevels getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(AlarmLevels alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public int getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(int scheduleType) {
        this.scheduleType = scheduleType;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getActiveYear() {
        return activeYear;
    }

    public void setActiveYear(int activeYear) {
        this.activeYear = activeYear;
    }

    public int getActiveMonth() {
        return activeMonth;
    }

    public void setActiveMonth(int activeMonth) {
        this.activeMonth = activeMonth;
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

    public int getActiveSecond() {
        return activeSecond;
    }

    public void setActiveSecond(int activeSecond) {
        this.activeSecond = activeSecond;
    }

    public String getActiveCron() {
        return activeCron;
    }

    public void setActiveCron(String activeCron) {
        this.activeCron = activeCron;
    }

    public int getInactiveYear() {
        return inactiveYear;
    }

    public void setInactiveYear(int inactiveYear) {
        this.inactiveYear = inactiveYear;
    }

    public int getInactiveMonth() {
        return inactiveMonth;
    }

    public void setInactiveMonth(int inactiveMonth) {
        this.inactiveMonth = inactiveMonth;
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

    public int getInactiveSecond() {
        return inactiveSecond;
    }

    public void setInactiveSecond(int inactiveSecond) {
        this.inactiveSecond = inactiveSecond;
    }

    public String getInactiveCron() {
        return inactiveCron;
    }

    public void setInactiveCron(String inactiveCron) {
        this.inactiveCron = inactiveCron;
    }

    public int getTimeoutPeriods() {
        return timeoutPeriods;
    }

    public void setTimeoutPeriods(int timeoutPeriods) {
        this.timeoutPeriods = timeoutPeriods;
    }

    public int getTimeoutPeriodType() {
        return timeoutPeriodType;
    }

    public void setTimeoutPeriodType(int timeoutPeriodType) {
        this.timeoutPeriodType = timeoutPeriodType;
    }

    public MangoPermission getTogglePermission() {
        return togglePermission;
    }

    public void setTogglePermission(MangoPermission togglePermission) {
        this.togglePermission = togglePermission;
    }

    public EventTypeVO getEventType() {
        return new EventTypeVO(new MaintenanceEventType(id), getDescription(), alarmLevel);
    }

    public TranslatableMessage getDescription() {
        TranslatableMessage message;

        if (!StringUtils.isBlank(name)) {
            message = new TranslatableMessage("common.default", name);
        } else {
            //Hack together a name for the message
            String eventName = "N/A";

            //Single data point
            if((dataPoints.size() == 1) && (dataSources.size() == 0)) {
                DataPointVO vo = DataPointDao.getInstance().get(dataPoints.get(0));
                if(vo != null)
                    eventName = vo.getName();
            }else if((dataPoints.size() == 0) && (dataSources.size() == 1)){
                DataSourceVO vo = DataSourceDao.getInstance().get(dataSources.get(0));
                if(vo != null)
                    eventName = vo.getName();
            }else if((dataPoints.size() > 1) && (dataSources.size() == 0)){
                eventName = "Multiple points"; //TODO Better name/translation?
            }else if((dataPoints.size() == 0) && (dataSources.size() > 1)){
                eventName = "Multiple data sources"; //TODO Better name/translation?
            }else {
                eventName = "Multiple data points and sources"; //TODO Better name/translation?
            }

            if (scheduleType == TYPE_MANUAL)
                message = new TranslatableMessage("maintenanceEvents.schedule.manual", eventName);
            else if (scheduleType == TYPE_ONCE) {
                message = new TranslatableMessage("maintenanceEvents.schedule.onceUntil", eventName,
                        Functions.getTime(new DateTime(activeYear, activeMonth, activeDay, activeHour, activeMinute,
                                activeSecond, 0).getMillis()), Functions.getTime(new DateTime(inactiveYear, inactiveMonth,
                                        inactiveDay, inactiveHour, inactiveMinute, inactiveSecond, 0).getMillis()));
            }
            else if (scheduleType == TYPE_HOURLY) {
                String activeTime = StringUtils.leftPad(Integer.toString(activeMinute), 2, '0') + ":"
                        + StringUtils.leftPad(Integer.toString(activeSecond), 2, '0');
                message = new TranslatableMessage("maintenanceEvents.schedule.hoursUntil", eventName, activeTime,
                        StringUtils.leftPad(Integer.toString(inactiveMinute), 2, '0') + ":"
                                + StringUtils.leftPad(Integer.toString(inactiveSecond), 2, '0'));
            }
            else if (scheduleType == TYPE_DAILY)
                message = new TranslatableMessage("maintenanceEvents.schedule.dailyUntil", eventName, activeTime(),
                        inactiveTime());
            else if (scheduleType == TYPE_WEEKLY)
                message = new TranslatableMessage("maintenanceEvents.schedule.weeklyUntil", eventName, weekday(true),
                        activeTime(), weekday(false), inactiveTime());
            else if (scheduleType == TYPE_MONTHLY)
                message = new TranslatableMessage("maintenanceEvents.schedule.monthlyUntil", eventName,
                        monthday(true), activeTime(), monthday(false), inactiveTime());
            else if (scheduleType == TYPE_YEARLY)
                message = new TranslatableMessage("maintenanceEvents.schedule.yearlyUntil", eventName, monthday(true),
                        month(true), activeTime(), monthday(false), month(false), inactiveTime());
            else if (scheduleType == TYPE_CRON)
                message = new TranslatableMessage("maintenanceEvents.schedule.cronUntil", eventName, activeCron,
                        inactiveCron);
            else
                throw new ShouldNeverHappenException("Unknown schedule type: " + scheduleType);
        }
        return message;
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
        return "event.audit.maintenanceEvent";
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("xid", xid);
        writer.writeEntry("alias", name);
        writer.writeEntry("alarmLevel", alarmLevel.name());
        writer.writeEntry("scheduleType", TYPE_CODES.getCode(scheduleType));

        List<String> dataSourceXids = new ArrayList<>();
        //Validate that the ids are legit
        for(int i=0; i<dataSources.size(); i++) {
            String xid = DataSourceDao.getInstance().getXidById(dataSources.get(i));
            if(xid != null)
                dataSourceXids.add(xid);
        }
        if(dataSourceXids.size() > 0)
            writer.writeEntry("dataSourceXids", dataSourceXids);

        List<String> dataPointXids = new ArrayList<>();
        for(int i=0; i<dataPoints.size(); i++) {
            String xid = DataPointDao.getInstance().getXidById(dataPoints.get(i));
            if(xid != null)
                dataPointXids.add(xid);
        }
        if(dataPointXids.size() > 0)
            writer.writeEntry("dataPointXids", dataPointXids);
        if(scheduleType == TYPE_MANUAL && timeoutPeriods > 0) {
            writer.writeEntry("timeoutPeriods", timeoutPeriods);
            writer.writeEntry("timeoutPeriodType", Common.TIME_PERIOD_CODES.getCode(timeoutPeriodType));
        }
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        name = jsonObject.getString("alias");
        String text = jsonObject.getString("dataSourceXid");
        if (text != null) {
            Integer id = DataSourceDao.getInstance().getIdByXid(text);
            if (id == null)
                throw new TranslatableJsonException("emport.error.maintenanceEvent.invalid", "dataSourceXid", text);
            dataSources.add(id);
        }

        JsonArray jsonDataPoints = jsonObject.getJsonArray("dataPointXids");
        if(jsonDataPoints != null) {
            dataPoints.clear();
            for(JsonValue jv : jsonDataPoints) {
                String xid = jv.toString();
                Integer id = DataPointDao.getInstance().getIdByXid(xid);
                if (id == null)
                    throw new TranslatableJsonException("emport.error.missingPoint", xid);
                dataPoints.add(id);
            }
        }
        JsonArray jsonDataSources = jsonObject.getJsonArray("dataSourceXids");
        if(jsonDataSources != null) {
            dataSources.clear();
            for(JsonValue jv : jsonDataSources) {
                String xid = jv.toString();
                Integer id = DataSourceDao.getInstance().getIdByXid(xid);
                if (id == null)
                    throw new TranslatableJsonException("emport.error.missingPoint", xid);
                dataSources.add(id);
            }
        }

        text = jsonObject.getString("alarmLevel");
        if (text != null) {
            try {
                alarmLevel = AlarmLevels.fromName(text);
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new TranslatableJsonException("emport.error.maintenanceEvent.invalid", "alarmLevel", text,
                        Arrays.asList(AlarmLevels.values()));
            }
        }

        text = jsonObject.getString("scheduleType");
        if (text != null) {
            scheduleType = TYPE_CODES.getId(text);
            if (!TYPE_CODES.isValidId(scheduleType))
                throw new TranslatableJsonException("emport.error.maintenanceEvent.invalid", "scheduleType", text,
                        TYPE_CODES.getCodeList());
        }
        timeoutPeriods = jsonObject.getInt("timeoutPeriods", -1);
        text = jsonObject.getString("timeoutPeriodType");
        if(text != null) {
            timeoutPeriodType = Common.TIME_PERIOD_CODES.getId(text);
            if(!Common.TIME_PERIOD_CODES.isValidId(timeoutPeriodType))
                throw new TranslatableJsonException("emport.error.maintenanceEvent.invalid", "timeoutPeriodType", text,
                        Common.TIME_PERIOD_CODES.getCodeList());
        }
    }
}
