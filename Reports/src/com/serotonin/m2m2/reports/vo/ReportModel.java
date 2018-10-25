/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.reports.vo;

import java.util.List;

import com.serotonin.m2m2.reports.ReportModelDefinition;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel;

/**
 * @author Terry Packer
 */
public class ReportModel extends AbstractVoModel<ReportVO> {

    public ReportModel(ReportVO data) {
        super(data);
    }

    public ReportModel(){
        super(new ReportVO());
    }

    @Override
    public String getModelType() {
        return ReportModelDefinition.TYPE_NAME;
    }

    public int getUserId() {
        return this.data.getUserId();
    }

    public void setUserId(int userId) {
        this.data.setUserId(userId);
    }

    public List<ReportPointVO> getPoints() {
        return this.data.getPoints();
    }

    public void setPoints(List<ReportPointVO> points) {
        this.data.setPoints(points);
    }

    public String getTemplate() {
        return this.data.getTemplate();
    }

    public void setTemplate(String template) {
        this.data.setTemplate(template);
    }

    public int getIncludeEvents() {
        return this.data.getIncludeEvents();
    }

    public void setIncludeEvents(int includeEvents) {
        this.data.setIncludeEvents(includeEvents);
    }

    public boolean isIncludeUserComments() {
        return this.data.isIncludeUserComments();
    }

    public void setIncludeUserComments(boolean includeUserComments) {
        this.data.setIncludeUserComments(includeUserComments);
    }

    public int getDateRangeType() {
        return this.data.getDateRangeType();
    }

    public void setDateRangeType(int dateRangeType) {
        this.data.setDateRangeType(dateRangeType);
    }

    public int getRelativeDateType() {
        return this.data.getRelativeDateType();
    }

    public void setRelativeDateType(int relativeDateType) {
        this.data.setRelativeDateType(relativeDateType);
    }

    public int getPreviousPeriodCount() {
        return this.data.getPreviousPeriodCount();
    }

    public void setPreviousPeriodCount(int previousPeriodCount) {
        this.data.setPreviousPeriodCount(previousPeriodCount);
    }

    public int getPreviousPeriodType() {
        return this.data.getPreviousPeriodType();
    }

    public void setPreviousPeriodType(int previousPeriodType) {
        this.data.setPreviousPeriodType(previousPeriodType);
    }

    public int getPastPeriodCount() {
        return this.data.getPastPeriodCount();
    }

    public void setPastPeriodCount(int pastPeriodCount) {
        this.data.setPastPeriodCount(pastPeriodCount);
    }

    public int getPastPeriodType() {
        return this.data.getPastPeriodType();
    }

    public void setPastPeriodType(int pastPeriodType) {
        this.data.setPastPeriodType(pastPeriodType);
    }

    public boolean isFromNone() {
        return this.data.isFromNone();
    }

    public void setFromNone(boolean fromNone) {
        this.data.setFromNone(fromNone);
    }

    public int getFromYear() {
        return this.data.getFromYear();
    }

    public void setFromYear(int fromYear) {
        this.data.setFromYear(fromYear);
    }

    public int getFromMonth() {
        return this.data.getFromMonth();
    }

    public void setFromMonth(int fromMonth) {
        this.data.setFromMonth(fromMonth);
    }

    public int getFromDay() {
        return this.data.getFromDay();
    }

    public void setFromDay(int fromDay) {
        this.data.setFromDay(fromDay);
    }

    public int getFromHour() {
        return this.data.getFromHour();
    }

    public void setFromHour(int fromHour) {
        this.data.setFromHour(fromHour);
    }

    public int getFromMinute() {
        return this.data.getFromMinute();
    }

    public void setFromMinute(int fromMinute) {
        this.data.setFromMinute(fromMinute);
    }

    public boolean isToNone() {
        return this.data.isToNone();
    }

    public void setToNone(boolean toNone) {
        this.data.setToNone(toNone);
    }

    public int getToYear() {
        return this.data.getToYear();
    }

    public void setToYear(int toYear) {
        this.data.setToYear(toYear);
    }

    public int getToMonth() {
        return this.data.getToMonth();
    }

    public void setToMonth(int toMonth) {
        this.data.setToMonth(toMonth);
    }

    public int getToDay() {
        return this.data.getToDay();
    }

    public void setToDay(int toDay) {
        this.data.setToDay(toDay);
    }

    public int getToHour() {
        return this.data.getToHour();
    }

    public void setToHour(int toHour) {
        this.data.setToHour(toHour);
    }

    public int getToMinute() {
        return this.data.getToMinute();
    }

    public void setToMinute(int toMinute) {
        this.data.setToMinute(toMinute);
    }

    public boolean isSchedule() {
        return this.data.isSchedule();
    }

    public void setSchedule(boolean schedule) {
        this.data.setSchedule(schedule);
    }

    public int getSchedulePeriod() {
        return this.data.getSchedulePeriod();
    }

    public void setSchedulePeriod(int schedulePeriod) {
        this.data.setSchedulePeriod(schedulePeriod);
    }

    public String getScheduleCron() {
        return this.data.getScheduleCron();
    }

    public void setScheduleCron(String scheduleCron) {
        this.data.setScheduleCron(scheduleCron);
    }

    public int getRunDelayMinutes() {
        return this.data.getRunDelayMinutes();
    }

    public void setRunDelayMinutes(int runDelayMinutes) {
        this.data.setRunDelayMinutes(runDelayMinutes);
    }

    public boolean isEmail() {
        return this.data.isEmail();
    }

    public void setEmail(boolean email) {
        this.data.setEmail(email);
    }

    public List<RecipientListEntryBean> getRecipients() {
        return this.data.getRecipients();
    }

    public void setRecipients(List<RecipientListEntryBean> recipients) {
        this.data.setRecipients(recipients);
    }

    public boolean isIncludeData() {
        return this.data.isIncludeData();
    }

    public void setIncludeData(boolean includeData) {
        this.data.setIncludeData(includeData);
    }

    public boolean isZipData() {
        return this.data.isZipData();
    }

    public void setZipData(boolean zipData) {
        this.data.setZipData(zipData);
    }
}
