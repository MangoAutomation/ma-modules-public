/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports.web;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.timer.TimerTask;
import com.serotonin.timer.TimerTrigger;

/**
 * This is the scheduled task that queues reports in the background work queue at the correct moment.
 * 
 * @author Matthew Lohbihler
 */
public class ReportJob extends TimerTask {
    private static final Map<Integer, ReportJob> JOB_REGISTRY = new HashMap<Integer, ReportJob>();

    public static void scheduleReportJob(String host, int port, ReportVO report) {
        synchronized (JOB_REGISTRY) {
            // Ensure that there is no existing job.
            unscheduleReportJob(report);

            if (report.isSchedule()) {
                CronTimerTrigger trigger;
                if (report.getSchedulePeriod() == ReportVO.SCHEDULE_CRON) {
                    try {
                        trigger = new CronTimerTrigger(report.getScheduleCron());
                    }
                    catch (ParseException e) {
                        throw new ShouldNeverHappenException(e);
                    }
                }
                else
                    trigger = Common.getCronTrigger(report.getSchedulePeriod(), report.getRunDelayMinutes() * 60);

                ReportJob reportJob = new ReportJob(trigger, report, host, port);
                JOB_REGISTRY.put(report.getId(), reportJob);
                Common.timer.schedule(reportJob);
            }
        }
    }

    public static void unscheduleReportJob(ReportVO report) {
        synchronized (JOB_REGISTRY) {
            ReportJob reportJob = JOB_REGISTRY.remove(report.getId());
            if (reportJob != null)
                reportJob.cancel();
        }
    }

    private final ReportVO report;
    private final String host;
    private final int port;

    private ReportJob(TimerTrigger trigger, ReportVO report, String host, int port) {
        super(trigger, "Report Queue Job " + report.getXid(), "RPRT_Q_" + report.getXid(), 5);
        this.report = report;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run(long runtime) {
        ReportWorkItem.queueReport(host, port, report);
    }
}
