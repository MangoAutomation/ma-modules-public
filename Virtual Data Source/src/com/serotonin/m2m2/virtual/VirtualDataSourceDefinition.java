/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual;

import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.virtual.dwr.VirtualEditDwr;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceModel;
import com.serotonin.m2m2.virtual.vo.VirtualDataSourceVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;

public class VirtualDataSourceDefinition extends DataSourceDefinition {
	
	public static final String TYPE_NAME = "VIRTUAL";
    
	@Override
    public String getDataSourceTypeName() {
        return TYPE_NAME;
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

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.DataSourceDefinition#getModelClass()
	 */
	@Override
	public Class<? extends AbstractDataSourceModel<?>> getModelClass() {
		return VirtualDataSourceModel.class;
	}
}
