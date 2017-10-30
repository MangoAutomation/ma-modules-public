/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.reports;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.module.SystemInfoDefinition;

/**
 * 
 * @author Terry Packer
 */
public class ReportNoSqlDatabaseSizeSystemInfoDefinition extends SystemInfoDefinition<Long> {

	public final String KEY = "reportsNoSqlDatabaseSize";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.serotonin.m2m2.module.ReadOnlySettingDefinition#getName()
	 */
	@Override
	public String getKey() {
		return KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.serotonin.m2m2.module.ReadOnlySettingDefinition#getValue()
	 */
	@Override
	public Long getValue() {
		long noSqlSize = 0L;
		if (Common.databaseProxy.getNoSQLProxy() != null) {
			noSqlSize = Common.databaseProxy.getNoSQLProxy().getDatabaseSizeInBytes("reports");
		}
		return noSqlSize;
	}

    @Override
    public String getDescriptionKey() {
        return "reports.noSqlDatabaseSizeDesc";
    }
}
