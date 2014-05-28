/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.module.PurgeDefinition;
import com.serotonin.m2m2.util.DateUtils;
import com.serotonin.m2m2.web.dwr.SystemSettingsDwr;

public class ReportPurgeDefinition extends PurgeDefinition {
    private static final Log LOG = LogFactory.getLog(ReportPurgeDefinition.class);

    public static final String REPORT_PURGE_PERIOD_TYPE = "reports.REPORT_PURGE_PERIOD_TYPE";
    public static final String REPORT_PURGE_PERIODS = "reports.REPORT_PURGE_PERIODS";

    @Override
    public void execute(long runtime) {
        DateTime cutoff = DateUtils.truncateDateTime(new DateTime(runtime), Common.TimePeriods.DAYS);
        cutoff = DateUtils.minus(cutoff,
                SystemSettingsDao.getIntValue(REPORT_PURGE_PERIOD_TYPE, Common.TimePeriods.MONTHS),
                SystemSettingsDao.getIntValue(REPORT_PURGE_PERIODS, 1));

        int deleteCount = new ReportDao().purgeReportsBefore( System.currentTimeMillis()); //cutoff.getMillis());
        if (deleteCount > 0)
            LOG.info("Report purge ended, " + deleteCount + " report instances deleted");
    }
}
