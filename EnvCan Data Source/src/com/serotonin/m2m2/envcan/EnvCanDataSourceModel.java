/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.envcan;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractPollingDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class EnvCanDataSourceModel extends AbstractPollingDataSourceModel<EnvCanDataSourceVO>{
	
	public EnvCanDataSourceModel() {
		super();
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
	@JsonGetter("dataStartTime")
	public Date getDataStartTime() {
	    return new Date(this.data.getDataStartTime());
	}

	@JsonSetter("dataStartTime")
	public void setDataStartTime(Date dataStartTime) {
	    if(dataStartTime != null)
	        this.data.setDataStartTime(dataStartTime.getTime());
	}

    @Override
    public String getModelType() {
        return EnvCanDataSourceDefinition.DATA_SOURCE_TYPE;
    }


	
}
