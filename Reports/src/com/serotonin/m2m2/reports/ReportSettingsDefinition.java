/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.SystemSettingsDefinition;
import com.serotonin.m2m2.reports.web.ReportWorkItem;
import com.serotonin.m2m2.rt.maint.work.WorkItem;

public class ReportSettingsDefinition extends SystemSettingsDefinition {
    @Override
    public String getDescriptionKey() {
        return "header.reports";
    }

    @Override
    public String getSectionJspPath() {
        return "web/settings.jspf";
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#getDefaultValues()
	 */
	@Override
	public Map<String, Object> getDefaultValues() {
		Map<String, Object> defaults = new HashMap<String, Object>();
		defaults.put(ReportPurgeDefinition.REPORT_PURGE_PERIODS, 1);
		defaults.put(ReportPurgeDefinition.REPORT_PURGE_PERIOD_TYPE, Common.TimePeriods.MONTHS);
		defaults.put(ReportWorkItem.REPORT_WORK_ITEM_PRIORITY, WorkItem.PRIORITY_LOW);
		return defaults;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#convertToValueFromCode(java.lang.String, java.lang.String)
	 */
	@Override
	public Integer convertToValueFromCode(String key, String code) {
		Integer id;
		switch(key){
		case ReportPurgeDefinition.REPORT_PURGE_PERIOD_TYPE:
			id = Common.TIME_PERIOD_CODES.getId(code);
			if(id > -1)
				return id;
		break;
		case ReportWorkItem.REPORT_WORK_ITEM_PRIORITY:
			id = Common.WORK_ITEM_CODES.getId(code);
			if(id > -1)
				return id;
		break;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#convertToCodeFromValue(java.lang.String, java.lang.Integer)
	 */
	@Override
	public String convertToCodeFromValue(String key, Integer value) {
		switch(key){
		case ReportPurgeDefinition.REPORT_PURGE_PERIOD_TYPE:
			return Common.TIME_PERIOD_CODES.getCode(value);
		case ReportWorkItem.REPORT_WORK_ITEM_PRIORITY:
			return Common.WORK_ITEM_CODES.getCode(value);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.SystemSettingsDefinition#validateSettings(java.util.Map, com.serotonin.m2m2.i18n.ProcessResult)
	 */
	@Override
	public void validateSettings(Map<String, Object> settings, ProcessResult response) {
		//Validate the purge periods
		Object value = settings.get(ReportPurgeDefinition.REPORT_PURGE_PERIODS);
		if(value != null)
			if((Integer)value < 1)
				response.addContextualMessage(ReportPurgeDefinition.REPORT_PURGE_PERIODS, "validate.greaterThanZero");
		
		//Validate the purge period type
		value = settings.get(ReportPurgeDefinition.REPORT_PURGE_PERIOD_TYPE);
		if(value != null)
			if (!Common.TIME_PERIOD_CODES.isValidId((Integer)value, TimePeriods.MILLISECONDS, TimePeriods.SECONDS, TimePeriods.MINUTES, TimePeriods.HOURS))
				response.addContextualMessage(ReportPurgeDefinition.REPORT_PURGE_PERIOD_TYPE, "validate.invalidValue");

		//Validate the work Item Priority
		value = settings.get(ReportWorkItem.REPORT_WORK_ITEM_PRIORITY);
		if(value != null)
			if (!Common.WORK_ITEM_CODES.isValidId((Integer)value))
				response.addContextualMessage(ReportWorkItem.REPORT_WORK_ITEM_PRIORITY, "validate.invalidValue");

	}
}
