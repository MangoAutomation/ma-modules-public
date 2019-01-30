/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.internal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractPollingDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class InternalDataSourceModel extends AbstractPollingDataSourceModel<InternalDataSourceVO>{


	/**
	 * @param data
	 */
	public InternalDataSourceModel(InternalDataSourceVO data) {
		super(data);
	}

	public InternalDataSourceModel() {
		super();
	}	

	@Override
	public String getModelType() {
		return InternalDataSourceDefinition.DATA_SOURCE_TYPE;
	}
	
	@JsonGetter(value="createPointsPattern")
	public String getCreatePointsPattern() {
	    return this.data.getCreatePointsPattern();
	}
	
	@JsonSetter(value="createPointsPattern")
	public void setCreatePointsPattern(String createPointsPattern) {
	    this.data.setCreatePointsPattern(createPointsPattern);
	}
}
