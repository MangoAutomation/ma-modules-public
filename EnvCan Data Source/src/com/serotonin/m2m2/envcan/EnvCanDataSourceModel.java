/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.envcan;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class EnvCanDataSourceModel extends AbstractDataSourceModel<EnvCanDataSourceVO>{
	
	public EnvCanDataSourceModel() {
		super(new EnvCanDataSourceVO());
	}
	
	/**
	 * @param data
	 */
	public EnvCanDataSourceModel(EnvCanDataSourceVO data) {
		super(data);
	}

	
	@JsonGetter("stationId")
	public int getStationId() {
	    return this.data.getStationId();
	}

	@JsonSetter("stationId")
	public void setStationId(int stationId) {
	    this.data.setStationId(stationId);
	}



	
}
