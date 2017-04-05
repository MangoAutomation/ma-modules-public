/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.serotonin.InvalidArgumentException;
import com.serotonin.io.StreamUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.LicenseViolatedException;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.email.PostEmailRunnable;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.reports.ReportDao;
import com.serotonin.m2m2.reports.ReportLicenseChecker;
import com.serotonin.m2m2.reports.vo.ReportInstance;
import com.serotonin.m2m2.reports.vo.ReportPointVO;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.reports.web.ReportChartCreator.PointStatistics;
import com.serotonin.m2m2.rt.maint.work.EmailWorkItem;
import com.serotonin.m2m2.rt.maint.work.WorkItem;
import com.serotonin.m2m2.util.chart.ImageChartUtils;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.util.ColorUtils;
import com.serotonin.web.mail.EmailAttachment;
import com.serotonin.web.mail.EmailContent;
import com.serotonin.web.mail.EmailInline;

/**
 * This is a report work item that runs a report. It is added to the work queue by the ReportJob class.
 * 
 * @author Matthew Lohbihler
 */
public class ReportWorkItem implements WorkItem {
    static final Log LOG = LogFactory.getLog(ReportWorkItem.class);

    //Key For Settings
    public static final String REPORT_WORK_ITEM_PRIORITY = "reports.REPORT_WORK_ITEM_PRIORITY";

    //    private static UsageExpiryChecker USAGE_EXPIRY_CHECKER;
    
    public ReportWorkItem(String host, int port){
    	this.host = host;
    	this.port = port;
    }
    
    private String host;
    private int port;

    @Override
    public int getPriority() {
    	//Allow User To Choose
    	return SystemSettingsDao.getIntValue(REPORT_WORK_ITEM_PRIORITY, WorkItem.PRIORITY_LOW);
    }

    public static void queueReport(String host, int port, ReportVO report) {
        LOG.debug("Queuing report with id " + report.getId());

        // Verify that the user is not disabled.
        User user = UserDao.instance.getUser(report.getUserId());
        if (user.isDisabled())
            return;

        // User is ok. Continue...
        ReportWorkItem item = new ReportWorkItem(host, port);

        // Create the database record in process.
        item.reportConfig = report;
        ReportInstance reportInstance = new ReportInstance(report);

        item.user = user;
        item.reportDao = ReportDao.instance;
        item.reportDao.saveReportInstance(reportInstance);

        // Start the report work item out of process.
        item.reportInstance = reportInstance;
        Common.backgroundProcessing.addWorkItem(item);

        LOG.debug("Queued report with id " + report.getId() + ", instance id " + reportInstance.getId());
    }

    ReportVO reportConfig;
    private User user;
    private ReportDao reportDao;
    private ReportInstance reportInstance;
    List<File> filesToDelete = new ArrayList<File>();

    @Override
    public void execute() {
    	try {
    		ReportLicenseChecker.checkLicense();
    	} catch(LicenseViolatedException e) {
    		LOG.error("Your core license doesn't permit you to use the reports module.");
    		reportInstance.setReportStartTime(Common.timer.currentTimeMillis());
    		reportInstance.setReportEndTime(Common.timer.currentTimeMillis());
    		reportInstance.setRecordCount(-1);
    		reportDao.saveReportInstance(reportInstance);
    		return;
    	}

        LOG.debug("Running report with id " + reportConfig.getId() + ", instance id " + reportInstance.getId());

        reportInstance.setRunStartTime(System.currentTimeMillis());
        reportDao.saveReportInstance(reportInstance);
        Translations translations = Common.getTranslations();

        // Create a list of DataPointVOs to which the user has permission.
        DataPointDao dataPointDao = DataPointDao.instance;
        List<ReportDao.PointInfo> points = new ArrayList<ReportDao.PointInfo>(reportConfig.getPoints().size());
        for (ReportPointVO reportPoint : reportConfig.getPoints()) {
            DataPointVO point = dataPointDao.getDataPoint(reportPoint.getPointId());
            if (point != null && Permissions.hasDataPointReadPermission(user, point)) {
                String colour = null;
                try {
                    if (!StringUtils.isBlank(reportPoint.getColour()))
                        colour = ColorUtils.toHexString(reportPoint.getColour()).substring(1);
                }
                catch (InvalidArgumentException e) {
                    // Should never happen since the colour would have been validated on save, so just let it go 
                    // as null.
                }
                points.add(new ReportDao.PointInfo(point, colour, reportPoint.getWeight(), reportPoint
                        .isConsolidatedChart(), reportPoint.getPlotType()));
            }
        }

        int recordCount = 0;
        try {
            if (!points.isEmpty()){
                if(Common.databaseProxy.getNoSQLProxy() == null)
                	recordCount = reportDao.runReportSQL(reportInstance, points);
                else
                	recordCount = reportDao.runReportNoSQL(reportInstance, points);
            }
        }
        catch (RuntimeException e) {
            recordCount = -1;
            throw e;
        }
        catch (Throwable e) {
            recordCount = -1;
            throw new RuntimeException("Report instance failed", e);
        }
        finally {
            reportInstance.setRunEndTime(System.currentTimeMillis());
            reportInstance.setRecordCount(recordCount);
            reportDao.saveReportInstance(reportInstance);
        }

        if (reportConfig.isEmail()) {
            String inlinePrefix = "R" + System.currentTimeMillis() + "-" + reportInstance.getId() + "-";

            // TODO should we create different instances of the email based upon language and timezone?

            // We are creating an email from the result. Create the content.
            final ReportChartCreator creator = new ReportChartCreator(translations, TimeZone.getDefault());
            creator.createContent(host, port, reportInstance, reportDao, inlinePrefix, reportConfig.isIncludeData());

            // Create the to list
            Set<String> addresses = MailingListDao.instance.getRecipientAddresses(reportConfig.getRecipients(),
                    new DateTime(reportInstance.getReportStartTime()));
            String[] toAddrs = addresses.toArray(new String[0]);

            // Create the email content object.
            EmailContent emailContent = new EmailContent(null, creator.getHtml(), Common.UTF8);

            // Add the consolidated chart
            if (creator.getImageData() != null)
                emailContent
                        .addInline(new EmailInline.ByteArrayInline(inlinePrefix + ReportChartCreator.IMAGE_CONTENT_ID,
                                creator.getImageData(), ImageChartUtils.getContentType()));

            // Add the point charts
            for (PointStatistics pointStatistics : creator.getPointStatistics()) {
                if (pointStatistics.getImageData() != null)
                    emailContent.addInline(new EmailInline.ByteArrayInline(inlinePrefix
                            + pointStatistics.getChartName(), pointStatistics.getImageData(), ImageChartUtils
                            .getContentType()));
            }

            // Add optional images used by the template.
            for (String s : creator.getInlineImageList())
                addImage(emailContent, s);

            // Check if we need to attach the data.
            if (reportConfig.isIncludeData()) {
                addFileAttachment(emailContent, reportInstance.getName() + ".csv", creator.getExportFile());
                addFileAttachment(emailContent, reportInstance.getName() + "Events.csv", creator.getEventFile());
                addFileAttachment(emailContent, reportInstance.getName() + "Comments.csv", creator.getCommentFile());
            }

            PostEmailRunnable[] postEmail = null;
            if (reportConfig.isIncludeData()) {
                // See that the temp file(s) gets deleted after the email is sent.
            	PostEmailRunnable deleteTempFile = new PostEmailRunnable() {
                    @Override
                    public void run() {
                        for (File file : filesToDelete) {
                            if (!file.delete())
                                LOG.warn("Temp file " + file.getPath() + " not deleted");
                        }
                    }
                };
                postEmail = new PostEmailRunnable[] { deleteTempFile };
            }

            try {
                TranslatableMessage lm = new TranslatableMessage("ftl.scheduledReport", reportConfig.getName());
                String subject = creator.getSubject();
                if (subject == null)
                    subject = lm.translate(translations);
                EmailWorkItem.queueEmail(toAddrs, subject, emailContent, postEmail);
            }
            catch (AddressException e) {
                LOG.error(e);
            }
            
            if(reportConfig.isSchedule()){
	            // Delete the report instance.
	            reportDao.deleteReportInstance(reportInstance.getId(), user.getId());
            }
        }

        LOG.debug("Finished running report with id " + reportConfig.getId() + ", instance id " + reportInstance.getId());
    }

    private void addImage(EmailContent emailContent, String imagePath) {
        emailContent.addInline(new EmailInline.FileInline(imagePath, Common.getWebPath(imagePath)));
    }

    private void addFileAttachment(EmailContent emailContent, String name, File file) {
        if (file != null) {
            if (reportConfig.isZipData()) {
                try {
                    File zipFile = File.createTempFile("tempZIP", ".zip");
                    ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
                    zipOut.putNextEntry(new ZipEntry(name));

                    FileInputStream in = new FileInputStream(file);
                    StreamUtils.transfer(in, zipOut);
                    in.close();

                    zipOut.closeEntry();
                    zipOut.close();

                    emailContent.addAttachment(new EmailAttachment.FileAttachment(name + ".zip", zipFile));

                    filesToDelete.add(zipFile);
                }
                catch (IOException e) {
                    LOG.error("Failed to create zip file", e);
                }
            }
            else
                emailContent.addAttachment(new EmailAttachment.FileAttachment(name, file));

            filesToDelete.add(file);
        }
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.maint.work.WorkItem#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Generating report: " + this.reportInstance.getName();
	}
}
