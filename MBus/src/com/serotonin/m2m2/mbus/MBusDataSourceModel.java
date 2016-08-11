/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.mbus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
public class MBusDataSourceModel extends AbstractDataSourceModel<MBusDataSourceVO>{

	private MBusDataSourceVO data;
	/**
	 * @param data
	 */
	public MBusDataSourceModel(MBusDataSourceVO data) {
		super(data);
		this.data = data;
                throw new RuntimeException("IMPLEMENT ME");
	}

	public MBusDataSourceModel() {
		super(new MBusDataSourceVO());
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return MBusDataSourceDefinition.DATA_SOURCE_TYPE;
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
