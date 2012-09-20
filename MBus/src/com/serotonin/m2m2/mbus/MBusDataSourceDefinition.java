/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.mbus.dwr.MBusEditDwr;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.license.DataSourceTypePointsLimit;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class MBusDataSourceDefinition extends DataSourceDefinition {
    @Override
    public void preInitialize() {
        ModuleRegistry.addLicenseEnforcement(new DataSourceTypePointsLimit(getModule().getName(), "MBUS", 20, null));
    }

    @Override
    public String getDataSourceTypeName() {
        return "MBUS";
    }

    @Override
    public String getDescriptionKey() {
        return "dsEdit.mbus";
    }

    @Override
    protected DataSourceVO<?> createDataSourceVO() {
        return new MBusDataSourceVO();
    }

    @Override
    public String getEditPagePath() {
        return "web/editMBus.jsp";
    }

    @Override
    public Class<?> getDwrClass() {
        return MBusEditDwr.class;
    }
}
