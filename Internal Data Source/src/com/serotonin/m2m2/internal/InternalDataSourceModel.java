/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class InternalDataSourceModel extends AbstractDataSourceModel<InternalDataSourceVO>{

	/**
	 * @param data
	 */
	public InternalDataSourceModel(DataSourceVO<InternalDataSourceVO> data) {
		super(data);
	}

	public InternalDataSourceModel() {
		super(new InternalDataSourceVO());
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
