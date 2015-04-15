/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.vmstat;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

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
	
	@JsonGetter(value="pollPeriod")
	public TimePeriod getPollPeriod(){
	    return new TimePeriod(this.data.getPollSeconds(), TimePeriodType.convertTo(Common.TimePeriods.SECONDS));
	}

	@JsonSetter(value="pollPeriod")
	public void setPollPeriod(TimePeriod pollPeriod){ //Cannot change from seconds
	    this.data.setPollSeconds(pollPeriod.getPeriods());
	}

	@JsonGetter("outputScale")
	public String getOutputScale() {
	    return VMStatDataSourceVO.OUTPUT_SCALE_CODES.getCode(this.data.getOutputScale());
	}

	@JsonSetter("outputScale")
	public void setOutputScale(String outputScale) {
	    this.data.setOutputScale(VMStatDataSourceVO.OUTPUT_SCALE_CODES.getId(outputScale));
	}



}
