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
		VirtualDataSourceVO vo = ((VirtualDataSourceVO) this.data);
		
		return new TimePeriod(vo.getUpdatePeriods(), 
				TimePeriodType.convertTo(vo.getUpdatePeriodType()));
	}

	@JsonSetter(value="pollPeriod")
	public void setPollPeriod(TimePeriod pollPeriod){
		((VirtualDataSourceVO) this.data).setUpdatePeriods(pollPeriod.getPeriods());
		((VirtualDataSourceVO) this.data).setUpdatePeriodType(TimePeriodType.convertFrom(pollPeriod.getType()));
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
