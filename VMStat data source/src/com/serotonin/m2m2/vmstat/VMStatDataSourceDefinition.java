/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.vmstat;

import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class VMStatDataSourceDefinition extends DataSourceDefinition {
	
	public static final String DATA_SOURCE_TYPE = "VMSTAT";
	
    @Override
    public String getDataSourceTypeName() {
        return DATA_SOURCE_TYPE;
    }

    @Override
    public String getDescriptionKey() {
        return "dsEdit.vmstat";
    }

    @Override
    protected DataSourceVO<?> createDataSourceVO() {
        return new VMStatDataSourceVO();
    }

    @Override
    public String getEditPagePath() {
        return "web/editVMStat.jsp";
    }

    @Override
    public Class<?> getDwrClass() {
        return VMStatEditDwr.class;
    }
}
