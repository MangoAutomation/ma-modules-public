/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual;

import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.virtual.dwr.VirtualEditDwr;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class VirtualDataSourceDefinition extends DataSourceDefinition {
    @Override
    public String getDataSourceTypeName() {
        return "VIRTUAL";
    }

    @Override
    public String getDescriptionKey() {
        return "VIRTUAL.dataSource";
    }

    @Override
    public DataSourceVO<?> createDataSourceVO() {
        return new VirtualDataSourceVO();
    }

    @Override
    public String getEditPagePath() {
        return "web/editVirtual.jspf";
    }

    @Override
    public Class<?> getDwrClass() {
        return VirtualEditDwr.class;
    }
}
