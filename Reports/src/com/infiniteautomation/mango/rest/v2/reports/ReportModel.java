/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.reports;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.rest.v2.model.AbstractVoModel;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.reports.vo.ReportPointVO;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataPoint.TimePeriodModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.email.EmailRecipientModel;

/**
 * @author Terry Packer
 *
 */
public class ReportModel extends AbstractVoModel<ReportVO> {

    private String username;
    private List<ReportPointModel> points;
    private String template;

    private String includeEvents;
    private boolean includeUserComments;

    private String dateRangeType;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String relativeDateType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TimePeriodModel relativePeriod;

    //TODO Discuss if we need fromNone as we can just assume that 
    // if the date range type is SPECIFIC and from is null fromNone is true.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ZonedDateTime from;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ZonedDateTime to;

    private boolean schedule;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String schedulePeriod;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String scheduleCron;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer runDelayMinutes;

    private boolean email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<EmailRecipientModel<?>> recipients;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean includeData;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean zipData;

    public ReportModel() {
        super();
    }

    public ReportModel(ReportVO vo) {
        fromVO(vo);
    }
    
    /**
     * @return the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param user the user to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the points
     */
    public List<ReportPointModel> getPoints() {
        return points;
    }

    /**
     * @param points the points to set
     */
    public void setPoints(List<ReportPointModel> points) {
        this.points = points;
    }

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * @return the includeEvents
     */
    public String getIncludeEvents() {
        return includeEvents;
    }

    /**
     * @param includeEvents the includeEvents to set
     */
    public void setIncludeEvents(String includeEvents) {
        this.includeEvents = includeEvents;
    }

    /**
     * @return the includeUserComments
     */
    public boolean isIncludeUserComments() {
        return includeUserComments;
    }

    /**
     * @param includeUserComments the includeUserComments to set
     */
    public void setIncludeUserComments(boolean includeUserComments) {
        this.includeUserComments = includeUserComments;
    }

    /**
     * @return the dateRangeType
     */
    public String getDateRangeType() {
        return dateRangeType;
    }

    /**
     * @param dateRangeType the dateRangeType to set
     */
    public void setDateRangeType(String dateRangeType) {
        this.dateRangeType = dateRangeType;
    }

    /**
     * @return the relativeDateType
     */
    public String getRelativeDateType() {
        return relativeDateType;
    }

    /**
     * @param relativeDateType the relativeDateType to set
     */
    public void setRelativeDateType(String relativeDateType) {
        this.relativeDateType = relativeDateType;
    }

    /**
     * @return the relativePeriod
     */
    public TimePeriodModel getRelativePeriod() {
        return relativePeriod;
    }

    /**
     * @param relativePeriod the relativePeriod to set
     */
    public void setRelativePeriod(TimePeriodModel relativePeriod) {
        this.relativePeriod = relativePeriod;
    }

    /**
     * @return the from
     */
    public ZonedDateTime getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(ZonedDateTime from) {
        this.from = from;
    }

    /**
     * @return the to
     */
    public ZonedDateTime getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(ZonedDateTime to) {
        this.to = to;
    }

    /**
     * @return the schedule
     */
    public boolean isSchedule() {
        return schedule;
    }

    /**
     * @param schedule the schedule to set
     */
    public void setSchedule(boolean schedule) {
        this.schedule = schedule;
    }

    /**
     * @return the schedulePeriod
     */
    public String getSchedulePeriod() {
        return schedulePeriod;
    }

    /**
     * @param schedulePeriod the schedulePeriod to set
     */
    public void setSchedulePeriod(String schedulePeriod) {
        this.schedulePeriod = schedulePeriod;
    }

    /**
     * @return the scheduleCron
     */
    public String getScheduleCron() {
        return scheduleCron;
    }

    /**
     * @param scheduleCron the scheduleCron to set
     */
    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }

    /**
     * @return the runDelayMinutes
     */
    public Integer getRunDelayMinutes() {
        return runDelayMinutes;
    }

    /**
     * @param runDelayMinutes the runDelayMinutes to set
     */
    public void setRunDelayMinutes(Integer runDelayMinutes) {
        this.runDelayMinutes = runDelayMinutes;
    }

    /**
     * @return the email
     */
    public boolean isEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(boolean email) {
        this.email = email;
    }

    /**
     * @return the recipients
     */
    public List<EmailRecipientModel<?>> getRecipients() {
        return recipients;
    }

    /**
     * @param recipients the recipients to set
     */
    public void setRecipients(List<EmailRecipientModel<?>> recipients) {
        this.recipients = recipients;
    }

    /**
     * @return the includeData
     */
    public Boolean getIncludeData() {
        return includeData;
    }

    /**
     * @param includeData the includeData to set
     */
    public void setIncludeData(Boolean includeData) {
        this.includeData = includeData;
    }

    /**
     * @return the zipData
     */
    public Boolean getZipData() {
        return zipData;
    }

    /**
     * @param zipData the zipData to set
     */
    public void setZipData(Boolean zipData) {
        this.zipData = zipData;
    }

    @Override
    protected ReportVO newVO() {
        return new ReportVO();
    }

    @Override
    public void fromVO(ReportVO vo) {
        super.fromVO(vo);
        User user = UserDao.getInstance().getUser(vo.getUserId());
        if(user != null)
            this.username = user.getUsername();
        
        if(vo.getPoints() != null && vo.getPoints().size() > 0) {
            points = new ArrayList<>();
            for(ReportPointVO point : vo.getPoints())
                points.add(new ReportPointModel(point));
        }
        this.template = vo.getTemplate();
        this.includeEvents = ReportVO.EVENT_CODES.getCode(vo.getIncludeEvents());
        this.includeUserComments = vo.isIncludeUserComments();
        this.dateRangeType = ReportVO.DATE_RANGE_TYPES.getCode(vo.getDateRangeType());
        this.relativeDateType = ReportVO.DATE_RELATIVE_TYPES.getCode(vo.getRelativeDateType());
        switch(vo.getDateRangeType()) {
            case ReportVO.DATE_RANGE_TYPE_RELATIVE:
                switch(vo.getRelativeDateType()) {
                    case ReportVO.RELATIVE_DATE_TYPE_PAST:
                        this.relativePeriod = new TimePeriodModel(vo.getPastPeriodCount(), vo.getPastPeriodType());
                        break;
                    case ReportVO.RELATIVE_DATE_TYPE_PREVIOUS:
                        this.relativePeriod = new TimePeriodModel(vo.getPreviousPeriodCount(), vo.getPreviousPeriodType());
                    default:
                        break;
                }
                break;
            case ReportVO.DATE_RANGE_TYPE_SPECIFIC:
                //TODO Use timezone of http user or report user?
                user = Common.getHttpUser();
                ZoneId zone;
                if(user == null)
                    zone = TimeZone.getDefault().toZoneId();
                else
                    zone = user.getTimeZoneInstance().toZoneId();
                this.from = ZonedDateTime.of(vo.getFromYear(), 
                        vo.getFromMonth(), 
                        vo.getFromDay(), 
                        vo.getFromHour(), 
                        vo.getFromMinute(), 0 , 0,
                        zone);
                this.to = ZonedDateTime.of(vo.getToYear(), 
                        vo.getToMonth(), 
                        vo.getToDay(), 
                        vo.getToHour(), 
                        vo.getToMinute(), 0 , 0,
                        zone);
                break;
        }
        
        schedule = vo.isSchedule();
        if(schedule) {
            schedulePeriod = Common.TIME_PERIOD_CODES.getCode(vo.getSchedulePeriod());
            scheduleCron = vo.getScheduleCron();
            runDelayMinutes = vo.getRunDelayMinutes();
        }
        

        email = vo.isEmail();
        if(email) {
            recipients = new ArrayList<>();
            for(RecipientListEntryBean r : vo.getRecipients()) {
                EmailRecipientModel<?> model = EmailRecipientModel.createModel(r);
                if(model != null)
                    recipients.add(model);
            }
            includeData = vo.isIncludeData();
            zipData = vo.isZipData();
        }

    }

    @Override
    public ReportVO toVO() {
        ReportVO vo = super.toVO();
        User user = UserDao.getInstance().getUser(username);
        if(user != null)
            vo.setUserId(user.getId());
        
        if(points != null) {
            List<ReportPointVO> pointVOs = new ArrayList<>();
            for(ReportPointModel model : points)
                    pointVOs.add(model.toVO());
            vo.setPoints(pointVOs);
        }
        
        
        vo.setTemplate(template);;
        vo.setIncludeEvents(ReportVO.EVENT_CODES.getId(includeEvents));
        vo.setIncludeUserComments(includeUserComments);
        vo.setDateRangeType(ReportVO.DATE_RANGE_TYPES.getId(dateRangeType));
        vo.setRelativeDateType(ReportVO.DATE_RELATIVE_TYPES.getId(relativeDateType));
        switch(vo.getDateRangeType()) {
            case ReportVO.DATE_RANGE_TYPE_RELATIVE:
                if(this.relativePeriod != null) {
                    switch(vo.getRelativeDateType()) {
                        case ReportVO.RELATIVE_DATE_TYPE_PAST:
                            vo.setPastPeriodCount(relativePeriod.getPeriods());
                            vo.setPastPeriodType(Common.TIME_PERIOD_CODES.getId(relativePeriod.getPeriodType()));
                            break;
                        case ReportVO.RELATIVE_DATE_TYPE_PREVIOUS:
                            vo.setPreviousPeriodCount(relativePeriod.getPeriods());
                            vo.setPreviousPeriodType(Common.TIME_PERIOD_CODES.getId(relativePeriod.getPeriodType()));
                        default:
                            break;
                    }
                }
                break;
            case ReportVO.DATE_RANGE_TYPE_SPECIFIC:
                if(this.from != null) {
                    vo.setFromYear(from.getYear());
                    //Values 1 - 12 (Same as legacy UI)
                    vo.setFromMonth(from.getMonthValue());
                    vo.setFromDay(from.getDayOfMonth());
                    vo.setFromHour(from.getHour());
                    vo.setFromMinute(from.getMinute());
                }
                if(this.to != null) {
                    vo.setToYear(to.getYear());
                    //Values 1 - 12 (Same as legacy UI)
                    vo.setToMonth(to.getMonthValue());
                    vo.setToDay(to.getDayOfMonth());
                    vo.setToHour(to.getHour());
                    vo.setToMinute(to.getMinute()); 
                }
                break;
        }
        
        vo.setSchedule(schedule);
        if(vo.isSchedule()) {
            if(schedulePeriod != null) {
                vo.setSchedulePeriod(Common.TIME_PERIOD_CODES.getId(schedulePeriod));
            }
            vo.setScheduleCron(scheduleCron);
            if(runDelayMinutes != null)
                vo.setRunDelayMinutes(runDelayMinutes);
        }
        vo.setEmail(email);
        if(email) {
            if(recipients != null) {
                for(EmailRecipientModel<?> model : recipients)
                    vo.getRecipients().add(EmailRecipientModel.createBean(model));
            }
            if(includeData != null)
                vo.setIncludeData(includeData);
            if(zipData != null)
                vo.setZipData(zipData);
        }
        
        return vo;
    }

}
