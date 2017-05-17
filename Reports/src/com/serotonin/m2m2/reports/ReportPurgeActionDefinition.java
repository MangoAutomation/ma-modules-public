/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.reports;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.module.SystemActionDefinition;
import com.serotonin.m2m2.util.DateUtils;
import com.serotonin.m2m2.util.timeout.SystemActionTask;
import com.serotonin.timer.OneTimeTrigger;

/**
 * Purge reports based on settings
 * @author Terry Packer
 */
public class ReportPurgeActionDefinition extends SystemActionDefinition{

	private final String KEY = "reportPurgeUsingSettings";
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemActionDefinition#getKey()
	 */
	@Override
	public String getKey() {
		return KEY;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemActionDefinition#getWorkItem(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	public SystemActionTask getTask(final JsonNode input) {
		return new Action();
	}
	
	/**
	 * Class to allow purging data in ordered tasks with a queue 
	 * of up to 5 waiting purges
	 * 
	 * @author Terry Packer
	 */
	class Action extends SystemActionTask{
		
		public Action(){
			super(new OneTimeTrigger(0l), "Purge Reports", "REPORT_PURGE", 5);
		}

		/* (non-Javadoc)
		 * @see com.serotonin.timer.Task#run(long)
		 */
		@Override
		public void runImpl(long runtime) {
	        DateTime cutoff = DateUtils.truncateDateTime(new DateTime(runtime), Common.TimePeriods.DAYS);
	        cutoff = DateUtils.minus(cutoff,
	                SystemSettingsDao.getIntValue(ReportPurgeDefinition.REPORT_PURGE_PERIOD_TYPE, Common.TimePeriods.MONTHS),
	                SystemSettingsDao.getIntValue(ReportPurgeDefinition.REPORT_PURGE_PERIODS, 1));

	        int cnt = ReportDao.instance.purgeReportsBefore(cutoff.getMillis());
	        if (cnt > 0)
	            LOG.info("Report purge ended, " + cnt + " report instances deleted");

			this.results.put("deleted", cnt);
		}
	}
}
