/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class MBusDataSourceDefinition extends DataSourceDefinition {
	
	public static final String DATA_SOURCE_TYPE = "MBUS";
	
    @Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    @Override
    public String getDescriptionKey() {
        return "dsEdit.mbus";
    }

    @Override
    protected DataSourceVO<?> createDataSourceVO() {
        return new MBusDataSourceVO();
    }
}
