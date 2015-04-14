/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.vmstat;

import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class VMStatDataSourceModel extends AbstractDataSourceModel<VMStatDataSourceVO>{

	/**
	 * @param data
	 */
	public VMStatDataSourceModel(VMStatDataSourceVO data) {
		super(data);
	}
	
	public VMStatDataSourceModel() {
		super(new VMStatDataSourceVO());
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return VMStatDataSourceDefinition.DATA_SOURCE_TYPE;
	}

}
