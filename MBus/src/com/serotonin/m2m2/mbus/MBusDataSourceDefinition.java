/*
 *   Copyright (C) 2010 Arne Pl\u00f6se
 *   @author Arne Pl\u00f6se
 */
package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.mbus.dwr.MBusEditDwr;
import com.serotonin.m2m2.mbus.rest.MBusDataSourceModel;
import com.serotonin.m2m2.module.DataSourceDefinition;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

public class MBusDataSourceDefinition extends DataSourceDefinition {
	
	public static final String DATA_SOURCE_TYPE = "MBUS";
	
    @Override
    public void preInitialize() { }

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

    @Override
    public String getEditPagePath() {
        return "web/editMBus.jsp";
    }

    @Override
    public Class<?> getDwrClass() {
        return MBusEditDwr.class;
    }

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.DataSourceDefinition#getModelClass()
	 */
	@Override
	public Class<? extends AbstractDataSourceModel<?>> getModelClass() {
		return MBusDataSourceModel.class;
	}
}
