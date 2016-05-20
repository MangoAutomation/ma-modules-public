/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.module.RuntimeManagerDefinition;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.reports.web.ReportJob;

public class RTMDefinition extends RuntimeManagerDefinition {
    private static final Log LOG = LogFactory.getLog(RTMDefinition.class);

    @Override
    public int getInitializationPriority() {
        return 20;
    }

    @Override
    public void initialize(boolean safe) {
        List<ReportVO> reports = ReportDao.instance.getReports();
        for (ReportVO report : reports) {
            try {
                ReportJob.scheduleReportJob(report);
            }
            catch (ShouldNeverHappenException e) {
                // Don't stop the startup if there is an error. Just log it.
                LOG.error("Error starting report " + report.getName(), e);
            }
        }
    }

    @Override
    public void terminate() {
        // no op
    }
}
