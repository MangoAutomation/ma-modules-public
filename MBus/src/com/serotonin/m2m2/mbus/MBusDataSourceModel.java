/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.mbus;

import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class MBusDataSourceModel extends AbstractDataSourceModel<MBusDataSourceVO>{

	/**
	 * @param data
	 */
	public MBusDataSourceModel(DataSourceVO<MBusDataSourceVO> data) {
		super(data);
	}

	public MBusDataSourceModel() {
		super(new MBusDataSourceVO());
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		// TODO Implement when we have a model, should be the TYPE_NAME in the Model Definition
		return null;
	}

}
