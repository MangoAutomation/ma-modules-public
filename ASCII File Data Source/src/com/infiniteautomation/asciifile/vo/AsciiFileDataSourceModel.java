/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.asciifile.vo;

import com.infiniteautomation.asciifile.AsciiFileDataSourceDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class AsciiFileDataSourceModel extends AbstractDataSourceModel<AsciiFileDataSourceVO>{

	public AsciiFileDataSourceModel() {
		super(new AsciiFileDataSourceVO());
	}
	
	/**
	 * @param data
	 */
	public AsciiFileDataSourceModel(AsciiFileDataSourceVO data) {
		super(data);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return AsciiFileDataSourceDefinition.DATA_SOURCE_TYPE;
	}

}
