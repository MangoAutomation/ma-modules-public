/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.reports;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.util.exception.ValidationException;
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

	private final String KEY = "reportPurge";
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
	public SystemActionTask getTaskImpl(final JsonNode input) {
	    boolean purgeAll = false;
	    if (input != null && input.has("purgeAll")) {
	        purgeAll = input.get("purgeAll").asBoolean();
	    }
		return new Action(purgeAll);
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemActionDefinition#getPermissionTypeName()
	 */
	@Override
	protected String getPermissionTypeName() {
		return ReportPurgeActionPermissionDefinition.PERMISSION;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemActionDefinition#validate(com.fasterxml.jackson.databind.JsonNode)
	 */
	@Override
	protected void validate(JsonNode input) throws ValidationException {
		return;
	}
	
	/**
	 * Class to allow purging data in ordered tasks with a queue 
	 * of up to 5 waiting purges
	 * 
	 * @author Terry Packer
	 */
	class Action extends SystemActionTask{
	    boolean purgeAll;
	    
		public Action(boolean purgeAll){
			super(new OneTimeTrigger(0l), "Purge Reports Status Poller", "REPORT_PURGE_POLLER", 5);
			this.purgeAll = purgeAll;
		}

		/* (non-Javadoc)
		 * @see com.serotonin.timer.Task#run(long)
		 */
		@Override
		public void runImpl(long runtime) {
	        DateTime cutoff = DateUtils.truncateDateTime(new DateTime(runtime), Common.TimePeriods.DAYS);
	        cutoff = DateUtils.minus(cutoff,
	                SystemSettingsDao.instance.getIntValue(ReportPurgeDefinition.REPORT_PURGE_PERIOD_TYPE),
	                SystemSettingsDao.instance.getIntValue(ReportPurgeDefinition.REPORT_PURGE_PERIODS));

	        int cnt = ReportDao.getInstance().purgeReportsBefore(purgeAll ? Common.timer.currentTimeMillis() : cutoff.getMillis());
	        if (cnt > 0)
	            LOG.info("Report purge ended, " + cnt + " report instances deleted");

			this.results.put("deleted", cnt);
		}
	}
}
