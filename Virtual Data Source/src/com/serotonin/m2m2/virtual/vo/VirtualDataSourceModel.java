/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.virtual.vo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
public class VirtualDataSourceModel extends AbstractDataSourceModel<VirtualDataSourceVO>{

	public VirtualDataSourceModel(){
		super(new VirtualDataSourceVO());
	}
	/**
	 * @param data
	 */
	public VirtualDataSourceModel(VirtualDataSourceVO data) {
		super(data);
	}

	@JsonGetter(value="pollPeriod")
	public TimePeriod getPollPeriod(){
		return new TimePeriod(this.data.getUpdatePeriods(), 
				TimePeriodType.convertTo(this.data.getUpdatePeriodType()));
	}

	@JsonSetter(value="pollPeriod")
	public void setPollPeriod(TimePeriod pollPeriod){
		this.data.setUpdatePeriods(pollPeriod.getPeriods());
		this.data.setUpdatePeriodType(TimePeriodType.convertFrom(pollPeriod.getType()));
	}
	
	@JsonGetter(value="polling")
	public boolean isPolling(){
		return this.data.isPolling();
	}
	
	@JsonSetter(value="polling")
	public void setPolling(boolean polling) {
		this.data.setPolling(polling);
	}

}
