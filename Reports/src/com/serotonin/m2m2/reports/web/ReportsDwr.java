/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports.web;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.InvalidArgumentException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.vo.ReportInstance;
import com.serotonin.m2m2.reports.vo.ReportPointVO;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.dwr.ModuleDwr;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;
import com.serotonin.m2m2.web.dwr.util.DwrPermission;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.util.ColorUtils;
import com.serotonin.validation.StringValidation;

/**
 * @author Matthew Lohbihler
 */
public class ReportsDwr extends ModuleDwr {
    @DwrPermission(user = true)
    public ProcessResult init() {
        ProcessResult response = new ProcessResult();
        ReportDao reportDao = new ReportDao();
        User user = Common.getUser();

        response.addData("points", getReadablePoints());
        response.addData("mailingLists", new MailingListDao().getMailingLists());
        response.addData("users", new UserDao().getUsers());
        response.addData("reports", reportDao.getReports(user.getId()));
        response.addData("instances", getReportInstances(user));
        response.addData("templates", getTemplateList());

        return response;
    }

    @DwrPermission(user = true)
    public ReportVO getReport(int id, boolean copy) {
        ReportVO report;
        if (id == Common.NEW_ID) {
            report = new ReportVO();
            report.setXid(new ReportDao().generateUniqueXid());
            report.setName(translate("common.newName"));
        }
        else {
            report = new ReportDao().getReport(id);

            if (copy) {
                report.setId(Common.NEW_ID);
                report.setName(TranslatableMessage.translate(getTranslations(), "common.copyPrefix", report.getName()));
            }

            ReportCommon.ensureReportPermission(Common.getUser(), report);
        }
        return report;
    }
    
	private String[] getTemplateList() {
    	File templateDir = new File(Common.MA_HOME + ModuleRegistry.getModule("reports").getDirectoryPath() + "/web/ftl/");
		String[] list = templateDir.list();
		return list;
	}

    @DwrPermission(user = true)
    public ProcessResult saveReport(int id, String name, String xid, List<ReportPointVO> points, String template, int includeEvents,
            boolean includeUserComments, int dateRangeType, int relativeDateType, int previousPeriodCount,
            int previousPeriodType, int pastPeriodCount, int pastPeriodType, boolean fromNone, int fromYear,
            int fromMonth, int fromDay, int fromHour, int fromMinute, boolean toNone, int toYear, int toMonth,
            int toDay, int toHour, int toMinute, boolean schedule, int schedulePeriod, int runDelayMinutes,
            String scheduleCron, boolean email, boolean includeData, boolean zipData,
            List<RecipientListEntryBean> recipients) {

        ProcessResult response = new ProcessResult();

        // Basic validation
        validateData(response, name, points, dateRangeType, relativeDateType, previousPeriodCount, pastPeriodCount);

        if (schedule) {
            if (schedulePeriod == ReportVO.SCHEDULE_CRON) {
                // Check the cron pattern.
                try {
                    new CronTimerTrigger(scheduleCron);
                }
                catch (Exception e) {
                    response.addContextualMessage("scheduleCron", "reports.validate.cron", e.getMessage());
                }
            }
            else {
                if (runDelayMinutes < 0)
                    response.addContextualMessage("runDelayMinutes", "reports.validate.lessThan0");
                else if (runDelayMinutes > 59)
                    response.addContextualMessage("runDelayMinutes", "reports.validate.greaterThan59");
            }
        }

        if (schedule && email && recipients.isEmpty())
            response.addContextualMessage("recipients", "reports.validate.needRecip");

        if (response.getHasMessages())
            return response;

        User user = Common.getUser();
        ReportDao reportDao = new ReportDao();
        ReportVO report;
        if (id == Common.NEW_ID) {
            report = new ReportVO();
            report.setUserId(user.getId());
        }
        else
            report = reportDao.getReport(id);
        
        ReportCommon.ensureReportPermission(user, report);

        // Update the new values.
        report.setXid(xid);
        report.setName(name);
        report.setPoints(points);
        report.setTemplate(template);
        report.setIncludeEvents(includeEvents);
        report.setIncludeUserComments(includeUserComments);
        report.setDateRangeType(dateRangeType);
        report.setRelativeDateType(relativeDateType);
        report.setPreviousPeriodCount(previousPeriodCount);
        report.setPreviousPeriodType(previousPeriodType);
        report.setPastPeriodCount(pastPeriodCount);
        report.setPastPeriodType(pastPeriodType);
        report.setFromNone(fromNone);
        report.setFromYear(fromYear);
        report.setFromMonth(fromMonth);
        report.setFromDay(fromDay);
        report.setFromHour(fromHour);
        report.setFromMinute(fromMinute);
        report.setToNone(toNone);
        report.setToYear(toYear);
        report.setToMonth(toMonth);
        report.setToDay(toDay);
        report.setToHour(toHour);
        report.setToMinute(toMinute);
        report.setSchedule(schedule);
        report.setSchedulePeriod(schedulePeriod);
        report.setRunDelayMinutes(runDelayMinutes);
        report.setScheduleCron(scheduleCron);
        report.setEmail(email);
        report.setIncludeData(includeData);
        report.setZipData(zipData);
        report.setRecipients(recipients);

        // Save the report
        reportDao.saveReport(report);

        // Conditionally schedule the report.
        ReportJob.scheduleReportJob(report);

        // Send back the report id in case this was new.
        response.addData("reportId", report.getId());
        return response;
    }

    @DwrPermission(user = true)
    public ProcessResult runReport(String xid, String name, List<ReportPointVO> points, String template, int includeEvents,
            boolean includeUserComments, int dateRangeType, int relativeDateType, int previousPeriodCount,
            int previousPeriodType, int pastPeriodCount, int pastPeriodType, boolean fromNone, int fromYear,
            int fromMonth, int fromDay, int fromHour, int fromMinute, boolean toNone, int toYear, int toMonth,
            int toDay, int toHour, int toMinute, boolean email, boolean includeData, boolean zipData,
            List<RecipientListEntryBean> recipients) {
        ProcessResult response = new ProcessResult();

        // Basic validation
        //TODO Replace with vo.validate()
        validateData(response, name, points, dateRangeType, relativeDateType, previousPeriodCount, pastPeriodCount);

        if (!response.getHasMessages()) {
            ReportVO report = new ReportVO();
            report.setXid(xid);
            report.setName(name);
            report.setUserId(Common.getUser().getId());
            report.setPoints(points);
            report.setTemplate(template);
            report.setIncludeEvents(includeEvents);
            report.setIncludeUserComments(includeUserComments);
            report.setDateRangeType(dateRangeType);
            report.setRelativeDateType(relativeDateType);
            report.setPreviousPeriodCount(previousPeriodCount);
            report.setPreviousPeriodType(previousPeriodType);
            report.setPastPeriodCount(pastPeriodCount);
            report.setPastPeriodType(pastPeriodType);
            report.setFromNone(fromNone);
            report.setFromYear(fromYear);
            report.setFromMonth(fromMonth);
            report.setFromDay(fromDay);
            report.setFromHour(fromHour);
            report.setFromMinute(fromMinute);
            report.setToNone(toNone);
            report.setToYear(toYear);
            report.setToMonth(toMonth);
            report.setToDay(toDay);
            report.setToHour(toHour);
            report.setToMinute(toMinute);
            report.setEmail(email);
            report.setIncludeData(includeData);
            report.setZipData(zipData);
            report.setRecipients(recipients);

            ReportWorkItem.queueReport(report);
        }

        return response;
    }

    @DwrPermission(user = true)
    public void deleteReport(int id) {
        ReportDao reportDao = new ReportDao();

        ReportVO report = reportDao.getReport(id);
        if (report != null) {
            ReportCommon.ensureReportPermission(Common.getUser(), report);
            ReportJob.unscheduleReportJob(report);
            reportDao.deleteReport(id);
        }
    }

    private void validateData(ProcessResult response, String name, List<ReportPointVO> points, int dateRangeType,
            int relativeDateType, int previousPeriodCount, int pastPeriodCount) {
        if (StringUtils.isBlank(name))
            response.addContextualMessage("name", "reports.validate.required");
        if (StringValidation.isLengthGreaterThan(name, 100))
            response.addContextualMessage("name", "reports.validate.longerThan100");
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

        User user = Common.getUser();
        DataPointDao dataPointDao = new DataPointDao();
        for (ReportPointVO point : points) {
            Permissions.ensureDataPointReadPermission(user, dataPointDao.getDataPoint(point.getPointId()));

            try {
                if (!StringUtils.isBlank(point.getColour()))
                    ColorUtils.toColor(point.getColour());
            }
            catch (InvalidArgumentException e) {
                response.addContextualMessage("points", "reports.validate.colour", point.getColour());
            }

            if (point.getWeight() <= 0)
                response.addContextualMessage("points", "reports.validate.weight");
        }
    }

    @DwrPermission(user = true)
    public List<ReportInstance> deleteReportInstance(int instanceId) {
        User user = Common.getUser();
        ReportDao reportDao = new ReportDao();
        reportDao.deleteReportInstance(instanceId, user.getId());
        return getReportInstances(user);
    }

    @DwrPermission(user = true)
    public List<ReportInstance> getReportInstances() {
        return getReportInstances(Common.getUser());
    }

    private List<ReportInstance> getReportInstances(User user) {
        List<ReportInstance> result = new ReportDao().getReportInstances(user.getId());
        Translations translations = getTranslations();
        for (ReportInstance i : result)
            i.setTranslations(translations);
        return result;
    }

    @DwrPermission(user = true)
    public void setPreventPurge(int instanceId, boolean value) {
        new ReportDao().setReportInstancePreventPurge(instanceId, value, Common.getUser().getId());
    }

    @DwrPermission(user = true)
    public ReportVO createReportFromWatchlist(String name, int[] dataPointIds) {
        ReportVO report = new ReportVO();
        User user = Common.getUser();

        report.setName(new TranslatableMessage("common.copyPrefix", name).translate(getTranslations()));
        report.setXid(Common.generateXid("REP_"));
        DataPointDao dataPointDao = new DataPointDao();
        for (int id : dataPointIds) {
            DataPointVO dp = dataPointDao.getDataPoint(id);
            if (dp == null || !Permissions.hasDataPointReadPermission(user, dp))
                continue;

            ReportPointVO rp = new ReportPointVO();
            rp.setPointId(dp.getId());
            rp.setPointKey("p" + dp.getId());
            rp.setColour(dp.getChartColour());
            rp.setConsolidatedChart(true);
            rp.setPlotType(dp.getPlotType());
            report.getPoints().add(rp);
        }

        return report;
    }
}
