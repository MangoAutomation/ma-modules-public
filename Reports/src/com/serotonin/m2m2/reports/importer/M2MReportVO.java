/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports.importer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonWriter;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.reports.vo.ReportPointVO;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.util.DateUtils;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;
import com.serotonin.util.SerializationHelper;

/**
 * @author Terry Packer
 *
 */
public class M2MReportVO implements Serializable{
	
	public static final int DATE_RANGE_TYPE_RELATIVE = 1;
    public static final int DATE_RANGE_TYPE_SPECIFIC = 2;

    public static final int EVENTS_NONE = 1;
    public static final int EVENTS_ALARMS = 2;
    public static final int EVENTS_ALL = 3;

    public static final int RELATIVE_DATE_TYPE_PREVIOUS = 1;
    public static final int RELATIVE_DATE_TYPE_PAST = 2;

    public static final int SCHEDULE_CRON = 0;

    private int id = Common.NEW_ID;
    private int userId;
    private String name;
    private List<M2MReportPointVO> points = new ArrayList<M2MReportPointVO>();
    private int includeEvents = EVENTS_ALARMS;
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
    private int runDelayMinutes;
    private String scheduleCron;

    private boolean email;
    private List<M2MRecipientListEntryBean> recipients = new ArrayList<M2MRecipientListEntryBean>();
    private boolean includeData = true;
    private boolean zipData = false;

    public M2MReportVO() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<M2MReportPointVO> getPoints() {
        return points;
    }

    public void setPoints(List<M2MReportPointVO> points) {
        this.points = points;
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

    public List<M2MRecipientListEntryBean> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<M2MRecipientListEntryBean> recipients) {
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

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 6;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);

        out.writeObject(points);
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
            points = convertToReportPointVOs((List<Integer>) in.readObject());
            includeEvents = EVENTS_ALARMS;
            includeUserComments = true;
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
            runDelayMinutes = 0;
            scheduleCron = SerializationHelper.readSafeUTF(in);
            email = in.readBoolean();
            recipients = (List<M2MRecipientListEntryBean>) in.readObject();
            includeData = in.readBoolean();
            zipData = false;
        }
        else if (ver == 2) {
            points = convertToReportPointVOs((List<Integer>) in.readObject());
            includeEvents = EVENTS_ALARMS;
            includeUserComments = true;
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
            recipients = (List<M2MRecipientListEntryBean>) in.readObject();
            includeData = in.readBoolean();
            zipData = false;
        }
        else if (ver == 3) {
            points = convertToReportPointVOs((List<Integer>) in.readObject());
            includeEvents = in.readInt();
            includeUserComments = true;
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
            recipients = (List<M2MRecipientListEntryBean>) in.readObject();
            includeData = in.readBoolean();
            zipData = false;
        }
        else if (ver == 4) {
            points = convertToReportPointVOs((List<Integer>) in.readObject());
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
            recipients = (List<M2MRecipientListEntryBean>) in.readObject();
            includeData = in.readBoolean();
            zipData = false;
        }
        else if (ver == 5) {
            points = (List<M2MReportPointVO>) in.readObject();
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
            recipients = (List<M2MRecipientListEntryBean>) in.readObject();
            includeData = in.readBoolean();
            zipData = false;
        }
        else if (ver == 6) {
            points = (List<M2MReportPointVO>) in.readObject();
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
            recipients = (List<M2MRecipientListEntryBean>) in.readObject();
            includeData = in.readBoolean();
            zipData = in.readBoolean();
        }
    }

    private static List<M2MReportPointVO> convertToReportPointVOs(List<Integer> ids) {
        List<M2MReportPointVO> result = new ArrayList<M2MReportPointVO>();
        for (Integer id : ids) {
            M2MReportPointVO vo = new M2MReportPointVO();
            vo.setPointId(id);
            result.add(vo);
        }
        return result;
    }

 	/**
	 * @return
	 */
	public ReportVO convert(M2MReportDao legacyDao) {
		ReportVO vo = new ReportVO();
		List<ReportPointVO> newPoints = new ArrayList<ReportPointVO>();
		for(M2MReportPointVO legacyPoint : this.points)
			newPoints.add(legacyPoint.convert(legacyDao));
		vo.setPoints(newPoints);
		
		//Find the user from M2M
		String username = legacyDao.getUsername(userId);
		User user = UserDao.instance.getUser(username);
		if(user != null)
			vo.setUserId(user.getId());
		else
			throw new ShouldNeverHappenException("Unable to find User " + username + " in Mango.");
		
		//Use default vo.setTemplate(template);
		vo.setIncludeEvents(includeEvents);
		vo.setIncludeUserComments(includeUserComments);
		vo.setDateRangeType(dateRangeType);
		vo.setRelativeDateType(relativeDateType);
		
		vo.setPreviousPeriodCount(previousPeriodCount);
		vo.setPreviousPeriodType(previousPeriodType);
		
		vo.setPastPeriodCount(pastPeriodCount);
		vo.setPastPeriodType(pastPeriodType);
		
		vo.setFromNone(fromNone);
		vo.setFromYear(fromYear);
		vo.setFromMonth(fromMonth);
		vo.setFromDay(fromDay);
		vo.setFromHour(fromHour);
		vo.setFromMinute(fromMinute);
		
		vo.setToNone(toNone);
		vo.setToYear(toYear);
		vo.setToMonth(toMonth);
		vo.setToDay(toDay);
		vo.setToHour(toHour);
		vo.setToMinute(toMinute);
		
		vo.setSchedule(schedule);
		vo.setSchedulePeriod(schedulePeriod);
		vo.setScheduleCron(scheduleCron);
		
		vo.setRunDelayMinutes(runDelayMinutes);
		
		vo.setEmail(email);
		
		//Convert the Recipient list
		List<RecipientListEntryBean> newRecipients = new ArrayList<RecipientListEntryBean>();
		for(M2MRecipientListEntryBean legacyRecipient: this.recipients)
			newRecipients.add(legacyRecipient.convert(legacyDao));
		
		vo.setIncludeData(includeData);
		vo.setZipData(zipData);
		
		return vo;
	}
	

	public void jsonWrite(JsonWriter jsonWriter, M2MReportDao legacyDao) throws IOException,
			JsonException {
		
		jsonWriter.indent();
		jsonWriter.append("{");
		jsonWriter.increaseIndent();

		//Write XID
		String xid = legacyDao.generateUniqueXid();
		writeEntry("xid", xid, jsonWriter, true, true);
		
		//Write Name
		writeEntry("name", name, jsonWriter, true, true);

		jsonWriter.indent();
		jsonWriter.quote("points");
		jsonWriter.append(": [");
		
		int cnt = 0;
		
		for(M2MReportPointVO point : points){

			point.jsonWrite(jsonWriter, legacyDao);
			cnt++;
			if(cnt < points.size())
				jsonWriter.append(",");
		}
		jsonWriter.append("],");
		
		writeEntry("user", legacyDao.getUsername(userId), jsonWriter, true, true);
		writeEntry("includeEvents", ReportVO.EVENT_CODES.getCode(includeEvents), jsonWriter, true, true);
		writeEntry("dateRangeType", ReportVO.DATE_RANGE_TYPES.getCode(dateRangeType), jsonWriter, true, true);
		
		if(dateRangeType == DATE_RANGE_TYPE_RELATIVE){
			writeEntry("relativeDateType", ReportVO.DATE_RELATIVE_TYPES.getCode(relativeDateType), jsonWriter, true, true);
			if(relativeDateType == RELATIVE_DATE_TYPE_PREVIOUS){
				writeEntry("perviousPeriodType", Common.TIME_PERIOD_CODES.getCode(previousPeriodType), jsonWriter, true, true);
				writeEntry("previousPeriods", Integer.toString(previousPeriodCount), jsonWriter, false, true);
			}else if(relativeDateType == RELATIVE_DATE_TYPE_PAST){
				writeEntry("pastPeriodType", Common.TIME_PERIOD_CODES.getCode(pastPeriodType), jsonWriter, true, true);
				writeEntry("pastPeriods", Integer.toString(pastPeriodCount), jsonWriter, false, true);
			}
		}else if(dateRangeType == DATE_RANGE_TYPE_SPECIFIC){
			writeEntry("fromInception", Boolean.toString(fromNone), jsonWriter, false, true);
			if(!fromNone){
				writeEntry("fromYear", Integer.toString(fromYear), jsonWriter, false, true);
				writeEntry("fromMonth", Integer.toString(fromMonth), jsonWriter, false, true);
				writeEntry("fromDay", Integer.toString(fromDay), jsonWriter, false, true);
				writeEntry("fromHour", Integer.toString(fromHour), jsonWriter, false, true);
				writeEntry("fromMinute", Integer.toString(fromMinute), jsonWriter, false, true);
			}
			writeEntry("toLatest", Boolean.toString(toNone), jsonWriter, false, true);
			if(!toNone){
				writeEntry("toYear", Integer.toString(toYear), jsonWriter, false, true);
				writeEntry("toMonth", Integer.toString(toMonth), jsonWriter, false, true);
				writeEntry("toDay", Integer.toString(toDay), jsonWriter, false, true);
				writeEntry("toHour", Integer.toString(toHour), jsonWriter, false, true);
				writeEntry("toMinute", Integer.toString(toMinute), jsonWriter, false, true);
			}			
			
		}
		
		writeEntry("schedule", Boolean.toString(schedule), jsonWriter, false, true);
		if(schedule){
			writeEntry("schedulePeriod", ReportVO.SCHEDULE_CODES.getCode(schedulePeriod), jsonWriter, true, true);
			if(schedulePeriod == SCHEDULE_CRON)
				writeEntry("scheduleCron", scheduleCron, jsonWriter, true, true);
		}
		
		writeEntry("runDelayMinutes", Integer.toString(runDelayMinutes), jsonWriter, false, true);
		
		writeEntry("email", Boolean.toString(email), jsonWriter, false, true);
		if(email){
			jsonWriter.indent();
			jsonWriter.quote("recipients");
			jsonWriter.append(": [");
			cnt=0;
			for(M2MRecipientListEntryBean entry : recipients){
				entry.jsonWrite(jsonWriter, legacyDao);
				cnt++;
				if(cnt < recipients.size())
					jsonWriter.append(",");
			}
			jsonWriter.append("],");
			
			writeEntry("includeData", Boolean.toString(includeData), jsonWriter, false, true);
			if(includeData)
				writeEntry("zipData", Boolean.toString(zipData), jsonWriter, false, true);
		}
		
		writeEntry("template", "reportChart.ftl", jsonWriter, true, false);

		jsonWriter.decreaseIndent();
		jsonWriter.indent();
		jsonWriter.append("}");
	}
	
	private void writeEntry(String name, String value, JsonWriter writer, boolean quote, boolean appendComma) throws IOException{
		writer.indent();
		writer.quote(name);
		writer.append(':');
		if(quote)
			writer.quote(value);
		else
			writer.append(value);
		
		if(appendComma)
			writer.append(",");
	}
	
}
