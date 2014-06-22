/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports.vo;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.serotonin.InvalidArgumentException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.util.DateUtils;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.AbstractVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.util.ColorUtils;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
public class ReportVO extends AbstractVO<ReportVO> implements Serializable, JsonSerializable {
	
	public static final String XID_PREFIX = "REPORT_";
	
    public static final int DATE_RANGE_TYPE_RELATIVE = 1;
    public static final int DATE_RANGE_TYPE_SPECIFIC = 2;
    public static final ExportCodes DATE_RANGE_TYPES = new ExportCodes();
    static{
    	DATE_RANGE_TYPES.addElement(DATE_RANGE_TYPE_RELATIVE, "RELATIVE");
    	DATE_RANGE_TYPES.addElement(DATE_RANGE_TYPE_SPECIFIC, "SPECIFIC");
    }


    public static final int EVENTS_NONE = 1;
    public static final int EVENTS_ALARMS = 2;
    public static final int EVENTS_ALL = 3;
    public static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
    	EVENT_CODES.addElement(EVENTS_NONE, "NONE");
    	EVENT_CODES.addElement(EVENTS_ALARMS, "ALARMS");
    	EVENT_CODES.addElement(EVENTS_ALL, "ALL");
    }
    
    public static final int RELATIVE_DATE_TYPE_PREVIOUS = 1;
    public static final int RELATIVE_DATE_TYPE_PAST = 2;

    public static final ExportCodes DATE_RELATIVE_TYPES = new ExportCodes();
    static {
    	DATE_RELATIVE_TYPES.addElement(RELATIVE_DATE_TYPE_PREVIOUS, "PREVIOUS");
    	DATE_RELATIVE_TYPES.addElement(RELATIVE_DATE_TYPE_PAST, "PAST");
    }
    
    
   
    public static final int SCHEDULE_CRON = 0;
    public static final ExportCodes SCHEDULE_CODES = new ExportCodes();
    static{
    	SCHEDULE_CODES.addElement(SCHEDULE_CRON, "CRON");
    	SCHEDULE_CODES.addElement(TimePeriods.YEARS, "YEAR");
    	SCHEDULE_CODES.addElement(TimePeriods.MONTHS, "MONTH");
    	SCHEDULE_CODES.addElement(TimePeriods.WEEKS, "WEEK");
    	SCHEDULE_CODES.addElement(TimePeriods.DAYS, "DAY");
    	SCHEDULE_CODES.addElement(TimePeriods.HOURS, "HOUR");
    	
    }
    
    private int userId;
    
    @JsonProperty
    private List<ReportPointVO> points = new ArrayList<ReportPointVO>();
    
    @JsonProperty
    private String template = "reportChart.ftl";
    
    private int includeEvents = EVENTS_ALARMS;
    @JsonProperty
    private boolean includeUserComments = true;
    
    private int dateRangeType = DATE_RANGE_TYPE_RELATIVE;
    private int relativeDateType = RELATIVE_DATE_TYPE_PREVIOUS;

    private int previousPeriodCount = 1;
    private int previousPeriodType = Common.TimePeriods.DAYS;

    private int pastPeriodCount = 1;
    private int pastPeriodType = Common.TimePeriods.DAYS;

    private boolean fromNone;
    private int fromYear;
    private int fromMonth;
    private int fromDay;
    private int fromHour;
    private int fromMinute;

    private boolean toNone;
    private int toYear;
    private int toMonth;
    private int toDay;
    private int toHour;
    private int toMinute;

    private boolean schedule;
    private int schedulePeriod = Common.TimePeriods.DAYS;
    private String scheduleCron;
    
    @JsonProperty
    private int runDelayMinutes;

    private boolean email;
    private List<RecipientListEntryBean> recipients = new ArrayList<RecipientListEntryBean>();
    private boolean includeData = true;
    private boolean zipData = false;

    public ReportVO() {
        // Default the specific date fields.
        DateTime dt = DateUtils.truncateDateTime(new DateTime(), Common.TimePeriods.DAYS);
        toYear = dt.getYear();
        toMonth = dt.getMonthOfYear();
        toDay = dt.getDayOfMonth();
        toHour = dt.getHourOfDay();
        toMinute = dt.getMinuteOfHour();

        dt = DateUtils.minus(dt, Common.TimePeriods.DAYS, 1);
        fromYear = dt.getYear();
        fromMonth = dt.getMonthOfYear();
        fromDay = dt.getDayOfMonth();
        fromHour = dt.getHourOfDay();
        fromMinute = dt.getMinuteOfHour();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<ReportPointVO> getPoints() {
        return points;
    }

    public void setPoints(List<ReportPointVO> points) {
        this.points = points;
    }
    
    public String getTemplate() {
    	return template;
    }
    
    public void setTemplate(String template) {
    	this.template = template;
    }

    public int getIncludeEvents() {
        return includeEvents;
    }

    public void setIncludeEvents(int includeEvents) {
        this.includeEvents = includeEvents;
    }

    public boolean isIncludeUserComments() {
        return includeUserComments;
    }

    public void setIncludeUserComments(boolean includeUserComments) {
        this.includeUserComments = includeUserComments;
    }

    public int getDateRangeType() {
        return dateRangeType;
    }

    public void setDateRangeType(int dateRangeType) {
        this.dateRangeType = dateRangeType;
    }

    public int getRelativeDateType() {
        return relativeDateType;
    }

    public void setRelativeDateType(int relativeDateType) {
        this.relativeDateType = relativeDateType;
    }

    public int getPreviousPeriodCount() {
        return previousPeriodCount;
    }

    public void setPreviousPeriodCount(int previousPeriodCount) {
        this.previousPeriodCount = previousPeriodCount;
    }

    public int getPreviousPeriodType() {
        return previousPeriodType;
    }

    public void setPreviousPeriodType(int previousPeriodType) {
        this.previousPeriodType = previousPeriodType;
    }

    public int getPastPeriodCount() {
        return pastPeriodCount;
    }

    public void setPastPeriodCount(int pastPeriodCount) {
        this.pastPeriodCount = pastPeriodCount;
    }

    public int getPastPeriodType() {
        return pastPeriodType;
    }

    public void setPastPeriodType(int pastPeriodType) {
        this.pastPeriodType = pastPeriodType;
    }

    public boolean isFromNone() {
        return fromNone;
    }

    public void setFromNone(boolean fromNone) {
        this.fromNone = fromNone;
    }

    public int getFromYear() {
        return fromYear;
    }

    public void setFromYear(int fromYear) {
        this.fromYear = fromYear;
    }

    public int getFromMonth() {
        return fromMonth;
    }

    public void setFromMonth(int fromMonth) {
        this.fromMonth = fromMonth;
    }

    public int getFromDay() {
        return fromDay;
    }

    public void setFromDay(int fromDay) {
        this.fromDay = fromDay;
    }

    public int getFromHour() {
        return fromHour;
    }

    public void setFromHour(int fromHour) {
        this.fromHour = fromHour;
    }

    public int getFromMinute() {
        return fromMinute;
    }

    public void setFromMinute(int fromMinute) {
        this.fromMinute = fromMinute;
    }

    public boolean isToNone() {
        return toNone;
    }

    public void setToNone(boolean toNone) {
        this.toNone = toNone;
    }

    public int getToYear() {
        return toYear;
    }

    public void setToYear(int toYear) {
        this.toYear = toYear;
    }

    public int getToMonth() {
        return toMonth;
    }

    public void setToMonth(int toMonth) {
        this.toMonth = toMonth;
    }

    public int getToDay() {
        return toDay;
    }

    public void setToDay(int toDay) {
        this.toDay = toDay;
    }

    public int getToHour() {
        return toHour;
    }

    public void setToHour(int toHour) {
        this.toHour = toHour;
    }

    public int getToMinute() {
        return toMinute;
    }

    public void setToMinute(int toMinute) {
        this.toMinute = toMinute;
    }

    public boolean isSchedule() {
        return schedule;
    }

    public void setSchedule(boolean schedule) {
        this.schedule = schedule;
    }

    public int getSchedulePeriod() {
        return schedulePeriod;
    }

    public void setSchedulePeriod(int schedulePeriod) {
        this.schedulePeriod = schedulePeriod;
    }

    public String getScheduleCron() {
        return scheduleCron;
    }

    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }

    public boolean isEmail() {
        return email;
    }

    public void setEmail(boolean email) {
        this.email = email;
    }

    public List<RecipientListEntryBean> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<RecipientListEntryBean> recipients) {
        this.recipients = recipients;
    }

    public boolean isIncludeData() {
        return includeData;
    }

    public void setIncludeData(boolean includeData) {
        this.includeData = includeData;
    }

    public boolean isZipData() {
        return zipData;
    }

    public void setZipData(boolean zipData) {
        this.zipData = zipData;
    }

    public int getRunDelayMinutes() {
        return runDelayMinutes;
    }

    public void setRunDelayMinutes(int runDelayMinutes) {
        this.runDelayMinutes = runDelayMinutes;
    }
    
    public Map<String, String> getXidMapping() {
    	DataPointDao dpd = new DataPointDao();
    	Map<String, String> ans = new HashMap<String, String>();
    	for(ReportPointVO vo : points) {
    		ans.put(dpd.get(vo.getPointId()).getXid(), vo.getPointKey());
    	}
    	return ans;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);

        out.writeObject(points);
        SerializationHelper.writeSafeUTF(out, template);
        out.writeInt(includeEvents);
        out.writeBoolean(includeUserComments);
        out.writeInt(dateRangeType);
        out.writeInt(relativeDateType);

        out.writeInt(previousPeriodCount);
        out.writeInt(previousPeriodType);
        out.writeInt(pastPeriodCount);
        out.writeInt(pastPeriodType);

        out.writeBoolean(fromNone);
        out.writeInt(fromYear);
        out.writeInt(fromMonth);
        out.writeInt(fromDay);
        out.writeInt(fromHour);
        out.writeInt(fromMinute);
        out.writeBoolean(toNone);
        out.writeInt(toYear);
        out.writeInt(toMonth);
        out.writeInt(toDay);
        out.writeInt(toHour);
        out.writeInt(toMinute);

        out.writeBoolean(schedule);
        out.writeInt(schedulePeriod);
        out.writeInt(runDelayMinutes);
        SerializationHelper.writeSafeUTF(out, scheduleCron);
        out.writeBoolean(email);
        out.writeObject(recipients);
        out.writeBoolean(includeData);
        out.writeBoolean(zipData);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            points = (List<ReportPointVO>) in.readObject();
            template = "reportChart.ftl";
            includeEvents = in.readInt();
            includeUserComments = in.readBoolean();
            dateRangeType = in.readInt();
            relativeDateType = in.readInt();

            previousPeriodCount = in.readInt();
            previousPeriodType = in.readInt();
            pastPeriodCount = in.readInt();
            pastPeriodType = in.readInt();

            fromNone = in.readBoolean();
            fromYear = in.readInt();
            fromMonth = in.readInt();
            fromDay = in.readInt();
            fromHour = in.readInt();
            fromMinute = in.readInt();
            toNone = in.readBoolean();
            toYear = in.readInt();
            toMonth = in.readInt();
            toDay = in.readInt();
            toHour = in.readInt();
            toMinute = in.readInt();

            schedule = in.readBoolean();
            schedulePeriod = in.readInt();
            runDelayMinutes = in.readInt();
            scheduleCron = SerializationHelper.readSafeUTF(in);
            email = in.readBoolean();
            recipients = (List<RecipientListEntryBean>) in.readObject();
            includeData = in.readBoolean();
            zipData = in.readBoolean();
        }
        
        else if (ver == 2) {
            points = (List<ReportPointVO>) in.readObject();
            template = SerializationHelper.readSafeUTF(in);
            includeEvents = in.readInt();
            includeUserComments = in.readBoolean();
            dateRangeType = in.readInt();
            relativeDateType = in.readInt();

            previousPeriodCount = in.readInt();
            previousPeriodType = in.readInt();
            pastPeriodCount = in.readInt();
            pastPeriodType = in.readInt();

            fromNone = in.readBoolean();
            fromYear = in.readInt();
            fromMonth = in.readInt();
            fromDay = in.readInt();
            fromHour = in.readInt();
            fromMinute = in.readInt();
            toNone = in.readBoolean();
            toYear = in.readInt();
            toMonth = in.readInt();
            toDay = in.readInt();
            toHour = in.readInt();
            toMinute = in.readInt();

            schedule = in.readBoolean();
            schedulePeriod = in.readInt();
            runDelayMinutes = in.readInt();
            scheduleCron = SerializationHelper.readSafeUTF(in);
            email = in.readBoolean();
            recipients = (List<RecipientListEntryBean>) in.readObject();
            includeData = in.readBoolean();
            zipData = in.readBoolean();
        }
    }

    @Override
    public void validate(ProcessResult response){
    	super.validate(response);
        if (points.isEmpty())
            response.addContextualMessage("points", "reports.validate.needPoint");
        
        if (dateRangeType != ReportVO.DATE_RANGE_TYPE_RELATIVE && dateRangeType != ReportVO.DATE_RANGE_TYPE_SPECIFIC)
            response.addGenericMessage("reports.validate.invalidDateRangeType");

        if (relativeDateType != ReportVO.RELATIVE_DATE_TYPE_PAST
                && relativeDateType != ReportVO.RELATIVE_DATE_TYPE_PREVIOUS)
            response.addGenericMessage("reports.validate.invalidRelativeDateType");
        if (previousPeriodCount < 1)
            response.addContextualMessage("previousPeriodCount", "reports.validate.periodCountLessThan1");
        if (pastPeriodCount < 1)
            response.addContextualMessage("pastPeriodCount", "reports.validate.periodCountLessThan1");
        
        UserDao dao = new UserDao();
        User user = dao.getUser(userId);
        if(user == null){
            response.addContextualMessage("userId", "reports.validate.userDNE");
        }
        
        File t = new File(Common.MA_HOME + ModuleRegistry.getModule("reports").getDirectoryPath() + "/web/ftl/" + template);
        if(!t.isFile())
        	response.addContextualMessage("template", "reports.validate.template");
        
        DataPointDao dataPointDao = new DataPointDao();
        for (ReportPointVO point : points) {
        	DataPointVO vo  = dataPointDao.getDataPoint(point.getPointId());
        	String pointXid = "unknown";
        	if(vo != null){
        		pointXid = vo.getXid();
            	try{
            		Permissions.ensureDataPointReadPermission(user, dataPointDao.getDataPoint(point.getPointId()));
            	}catch(PermissionException e){
            		
            		response.addContextualMessage("points", "reports.vaildate.pointDNE");
            	}
        	}else{
        		response.addContextualMessage("points", "reports.validate.pointPermissions",user.getUsername(), pointXid);
        	}
        		
            try {
                if (!StringUtils.isBlank(point.getColour()))
                    ColorUtils.toColor(point.getColour());
            }
            catch (InvalidArgumentException e) {
                response.addContextualMessage("points", "reports.validate.colour", point.getColour(), pointXid);
            }

            if (point.getWeight() <= 0)
                response.addContextualMessage("points", "reports.validate.weight");
        }
        
        //Validate the schedule
        if(schedule){
        	if(schedulePeriod == SCHEDULE_CRON){
        		try {
                    new CronTimerTrigger(scheduleCron);
                }catch (ParseException e) {
                    response.addContextualMessage("scheduleCron", "validate.invalidValue");
                }
        	}
        }
        
        
        
        
    }
    
    
    
    @Override
    public final void addProperties(List<TranslatableMessage> list) {
    	super.addProperties(list);

        AuditEventType.addPropertyMessage(list, "reports.points", points);
        AuditEventType.addPropertyMessage(list, "reports.template", template);
        AuditEventType.addExportCodeMessage(list, "reports.includeEvents", EVENT_CODES, includeEvents);
        AuditEventType.addPropertyMessage(list, "reports.comments", includeUserComments);
        AuditEventType.addExportCodeMessage(list, "reports.dateRangeType", DATE_RANGE_TYPES, dateRangeType);
        AuditEventType.addExportCodeMessage(list, "reports.relativeDateType",DATE_RELATIVE_TYPES, relativeDateType);
        AuditEventType.addPeriodMessage(list, "reports.previous", previousPeriodType, previousPeriodCount);
        AuditEventType.addPeriodMessage(list, "reports.past", pastPeriodType, pastPeriodCount);
        AuditEventType.addPropertyMessage(list, "reports.fromNone", fromNone);
        AuditEventType.addPropertyMessage(list, "reports.fromYear", fromYear);
        AuditEventType.addPropertyMessage(list, "reports.fromMonth", fromMonth);
        AuditEventType.addPropertyMessage(list, "reports.fromDay", fromDay);
        AuditEventType.addPropertyMessage(list, "reports.fromHour", fromHour);
        AuditEventType.addPropertyMessage(list, "reports.fromMinute", fromMinute);
        AuditEventType.addPropertyMessage(list, "reports.toNone", toNone);
        AuditEventType.addPropertyMessage(list, "reports.toYear", toYear);
        AuditEventType.addPropertyMessage(list, "reports.toMonth", toMonth);
        AuditEventType.addPropertyMessage(list, "reports.toDay", toDay);
        AuditEventType.addPropertyMessage(list, "reports.toHour", toHour);
        AuditEventType.addPropertyMessage(list, "reports.toMinute", toMinute);
        AuditEventType.addPropertyMessage(list, "reports.schedule", schedule);
        AuditEventType.addExportCodeMessage(list, "reports.scheulePeriod", SCHEDULE_CODES, schedulePeriod);
        AuditEventType.addPropertyMessage(list, "reports.runDelay", runDelayMinutes);
        AuditEventType.addPropertyMessage(list, "reports.cron", scheduleCron);
        AuditEventType.addPropertyMessage(list, "reports.emailReport", email);
        AuditEventType.addPropertyMessage(list, "reports.emailRecipients", recipients);
        AuditEventType.addPropertyMessage(list, "reports.includeTabular", includeData);
        AuditEventType.addPropertyMessage(list, "reports.zipData", zipData);
    }


    public void addPropertyChanges(List<TranslatableMessage> list, ReportVO from) {
    	super.addPropertyChanges(list,from);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.points", points, from.points);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.template", from.template, template);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "reports.includeEvents", EVENT_CODES, from.includeEvents, includeEvents);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.comments", from.includeUserComments, includeUserComments);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "reports.dateRangeType", DATE_RANGE_TYPES, from.dateRangeType, dateRangeType);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "reports.relativeDateType",DATE_RELATIVE_TYPES, from.relativeDateType, relativeDateType);
        AuditEventType.maybeAddPeriodChangeMessage(list, "reports.previous", from.previousPeriodType, from.previousPeriodCount, previousPeriodType, previousPeriodCount);
        AuditEventType.maybeAddPeriodChangeMessage(list, "reports.past", from.pastPeriodType, from.pastPeriodCount, pastPeriodType, pastPeriodCount);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.fromNone", from.fromNone, fromNone);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.fromYear", from.fromYear, fromYear);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.fromMonth", from.fromMonth, fromMonth);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.fromDay", from.fromDay, fromDay);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.fromHour", from.fromHour, fromHour);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.fromMinute", from.fromMinute, fromMinute);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.toNone", from.toNone, toNone);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.toYear", from.toYear, toYear);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.toMonth", from.toMonth, toMonth);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.toDay", from.toDay, toDay);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.toHour", from.toHour, toHour);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.toMinute", from.toMinute, toMinute);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.schedule", from.schedule, schedule);
        AuditEventType.maybeAddExportCodeChangeMessage(list, "reports.scheulePeriod", SCHEDULE_CODES, from.schedulePeriod, schedulePeriod);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.runDelay", from.runDelayMinutes, runDelayMinutes);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.cron", from.scheduleCron, scheduleCron);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.emailReport", from.email, email);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.emailRecipients", from.recipients, recipients);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.includeTabular", from.includeData, includeData);
        AuditEventType.maybeAddPropertyChangeMessage(list, "reports.zipData", from.zipData, zipData);
    }

    
	/* (non-Javadoc)
	 * @see com.serotonin.json.spi.JsonSerializable#jsonRead(com.serotonin.json.JsonReader, com.serotonin.json.type.JsonObject)
	 */
	@Override
	public void jsonRead(JsonReader reader, JsonObject jsonObject)
			throws JsonException {
		super.jsonRead(reader, jsonObject);
		
		String text = jsonObject.getString("includeEvents");
		if(text != null){
			includeEvents = EVENT_CODES.getId(text);
			if(includeEvents == -1)
				throw new TranslatableJsonException("emport.error.invalid",
						"includeEvents", text,
						EVENT_CODES.getCodeList());
		}
		
		text = jsonObject.getString("dateRangeType");
		if(text != null){
			dateRangeType = DATE_RANGE_TYPES.getId(text);
			if(dateRangeType == -1)
				throw new TranslatableJsonException("emport.error.invalid",
						"dateRangeType", text,
						DATE_RANGE_TYPES.getCodeList());
		}
		
		if(dateRangeType == DATE_RANGE_TYPE_RELATIVE){
			text = jsonObject.getString("relativeDateType");
			if(text != null){
				relativeDateType = DATE_RELATIVE_TYPES.getId(text);
				if(relativeDateType == -1)
					throw new TranslatableJsonException("emport.error.invalid",
							"relativeDateType", text,
							DATE_RELATIVE_TYPES.getCodeList());
			}
		
			if(relativeDateType == RELATIVE_DATE_TYPE_PREVIOUS ){
				text = jsonObject.getString("previousPeriodType");
				if(text != null){
					previousPeriodType = Common.TIME_PERIOD_CODES.getId(text);
					if(previousPeriodType == -1)
						throw new TranslatableJsonException("emport.error.invalid",
								"previousPeriodType", text,
								Common.TIME_PERIOD_CODES.getCodeList());
					previousPeriodCount = jsonObject.getInt("previousPeriods");
				}
			}else if(relativeDateType == RELATIVE_DATE_TYPE_PREVIOUS){
				text = jsonObject.getString("pastPeriodType");
				if(text != null){
					pastPeriodType = Common.TIME_PERIOD_CODES.getId(text);
					if(pastPeriodType == -1)
						throw new TranslatableJsonException("emport.error.invalid",
								"pastPeriodType", text,
								Common.TIME_PERIOD_CODES.getCodeList());
					pastPeriodCount = jsonObject.getInt("pastPeriods");
				}
			}
		}else if(dateRangeType == DATE_RANGE_TYPE_SPECIFIC){
			fromNone = jsonObject.getBoolean("fromInception");
			if(!fromNone){
				fromYear = jsonObject.getInt("fromYear");
				fromMonth = jsonObject.getInt("fromMonth");
				fromDay = jsonObject.getInt("fromDay");
				fromHour = jsonObject.getInt("fromHour");
				fromMinute = jsonObject.getInt("fromMinute");
				
			}
			toNone = jsonObject.getBoolean("toLatest");
			if(!toNone){
				toYear = jsonObject.getInt("toYear");
				toMonth = jsonObject.getInt("toMonth");
				toDay = jsonObject.getInt("toDay");
				toHour = jsonObject.getInt("toHour");
				toMinute = jsonObject.getInt("toMinute");
			}
		}
		
		schedule = jsonObject.getBoolean("schedule");
		if(schedule){
			text = jsonObject.getString("schedulePeriod");
			if(text != null){
				schedulePeriod = SCHEDULE_CODES.getId(text);
				if(schedulePeriod == -1)
					throw new TranslatableJsonException("emport.error.invalid",
							"schedulePeriod", text,
							SCHEDULE_CODES.getCodeList());
				if(schedulePeriod == SCHEDULE_CRON){
					scheduleCron = jsonObject.getString("scheduleCron");
					try {
	                    new CronTimerTrigger(scheduleCron);
	                }catch (ParseException e) {
	                	throw new TranslatableJsonException("emport.error.invalid",
								"scheduleCron", scheduleCron,
								"cron expressions");
	                }
				}
			}else{
				throw new TranslatableJsonException("emport.error.invalid",
						"schedulePeriod", "null",
						SCHEDULE_CODES.getCodeList());
			}
		}
		
		email = jsonObject.getBoolean("email");
		if(email){
			
			JsonArray recipientsArray = jsonObject.getJsonArray("recipients");
			boolean add = true;
			if(recipientsArray != null){
				for(JsonValue jv : recipientsArray){
					RecipientListEntryBean recipient = new RecipientListEntryBean();
					reader.readInto(recipient, jv);
					for(RecipientListEntryBean existing : recipients){
						if(existing.equals(recipient)){
							reader.readInto(existing, jv);
							add = false;
							break;
						}
					}
					if(add){
						recipients.add(recipient);
					}else{
						add = true;
					}
				}
			}else{
				throw new TranslatableJsonException("emport.error.invalid",
						"recipients", "null",
						"valid users, email addresses or mailing lists");
			}
			
			includeData = jsonObject.getBoolean("includeData");
			if(includeData)
				zipData = jsonObject.getBoolean("zipData");
				
		}
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.json.spi.JsonSerializable#jsonWrite(com.serotonin.json.ObjectWriter)
	 */
	@Override
	public void jsonWrite(ObjectWriter writer) throws IOException,
			JsonException {
		super.jsonWrite(writer);
		
		writer.writeEntry("includeEvents", EVENT_CODES.getCode(includeEvents));
		writer.writeEntry("dateRangeType", DATE_RANGE_TYPES.getCode(dateRangeType));
		
		if(dateRangeType == DATE_RANGE_TYPE_RELATIVE){
			writer.writeEntry("relativeDateType", DATE_RELATIVE_TYPES.getCode(relativeDateType));
			if(relativeDateType == RELATIVE_DATE_TYPE_PREVIOUS){
				writer.writeEntry("perviousPeriodType", Common.TIME_PERIOD_CODES.getCode(previousPeriodType));
				writer.writeEntry("previousPeriods", previousPeriodCount);
			}else if(relativeDateType == RELATIVE_DATE_TYPE_PAST){
				writer.writeEntry("pastPeriodType", Common.TIME_PERIOD_CODES.getCode(pastPeriodType));
				writer.writeEntry("pastPeriods", pastPeriodCount);
			}
		}else if(dateRangeType == DATE_RANGE_TYPE_SPECIFIC){
			writer.writeEntry("fromInception", fromNone);
			if(!fromNone){
				writer.writeEntry("fromYear", fromYear);
				writer.writeEntry("fromMonth", fromMonth);
				writer.writeEntry("fromDay", fromDay);
				writer.writeEntry("fromHour", fromHour);
				writer.writeEntry("fromMinute", fromMinute);
			}
			writer.writeEntry("toLatest", toNone);
			if(!toNone){
				writer.writeEntry("toYear", toYear);
				writer.writeEntry("toMonth", toMonth);
				writer.writeEntry("toDay", toDay);
				writer.writeEntry("toHour", toHour);
				writer.writeEntry("toMinute", toMinute);
			}			
			
		}
		
		writer.writeEntry("schedule", schedule);
		if(schedule){
			writer.writeEntry("schedulePeriod", SCHEDULE_CODES.getCode(schedulePeriod));
			if(schedulePeriod == SCHEDULE_CRON)
				writer.writeEntry("scheduleCron", scheduleCron);
		}
		
		writer.writeEntry("email", email);
		if(email){
			writer.writeEntry("recipients", recipients);
			writer.writeEntry("includeData", includeData);
			if(includeData)
				writer.writeEntry("zipData", zipData);
		}
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.util.ChangeComparable#getTypeKey()
	 */
	@Override
	public String getTypeKey() {
		return "event.audit.report";
	}
}
