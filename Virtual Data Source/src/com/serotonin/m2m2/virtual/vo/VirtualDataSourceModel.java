/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.virtual.vo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
public class VirtualDataSourceModel extends AbstractDataSourceModel<VirtualDataSourceVO>{

	private VirtualDataSourceVO vo;
	
	/**
	 * @param data
	 */
	public VirtualDataSourceModel(VirtualDataSourceVO data) {
		super(data);
		this.vo = data;
	}

	@JsonGetter(value="pollPeriod")
	public TimePeriod getPollPeriod(){
		return new TimePeriod(this.vo.getUpdatePeriods(), TimePeriodType.convertTo(this.vo.getUpdatePeriodType()));
	}

	@JsonSetter(value="pollPeriod")
	public void setPollPeriod(TimePeriod pollPeriod){
		this.vo.setUpdatePeriods(pollPeriod.getPeriods());
		this.vo.setUpdatePeriodType(TimePeriodType.convertFrom(pollPeriod.getType()));
	}

	
	
}
