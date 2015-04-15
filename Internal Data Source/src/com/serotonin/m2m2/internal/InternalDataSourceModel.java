/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.internal;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
public class InternalDataSourceModel extends AbstractDataSourceModel<InternalDataSourceVO>{

	private InternalDataSourceVO data;
	/**
	 * @param data
	 */
	public InternalDataSourceModel(InternalDataSourceVO data) {
		super(data);
		this.data = data;
	}

	public InternalDataSourceModel() {
		super(new InternalDataSourceVO());
	}	

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return InternalDataSourceDefinition.DATA_SOURCE_TYPE;
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



}
