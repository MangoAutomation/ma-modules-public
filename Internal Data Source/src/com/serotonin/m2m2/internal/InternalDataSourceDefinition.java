/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class InternalDataSourceDefinition extends DataSourceDefinition {
	public static final String DATA_SOURCE_TYPE = "INTERNAL";
    @Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    @Override
    public String getDescriptionKey() {
        return "dsEdit.internal";
    }

    @Override
    protected DataSourceVO createDataSourceVO() {
        return new InternalDataSourceVO();
    }

}
