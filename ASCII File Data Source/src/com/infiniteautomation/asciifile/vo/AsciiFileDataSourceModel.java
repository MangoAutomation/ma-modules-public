/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.asciifile.vo;
import com.infiniteautomation.asciifile.AsciiFileDataSourceDefinition;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriod;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

/**
 * @author Terry Packer
 *
 */
public class AsciiFileDataSourceModel extends AbstractDataSourceModel<AsciiFileDataSourceVO>{

	private AsciiFileDataSourceVO data;
	
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

	@JsonGetter("filePath")
	public String getFilePath() {
	    return this.data.getFilePath();
	}

	@JsonSetter("filePath")
	public void setFilePath(String filePath) {
	    this.data.setFilePath(filePath);
	}



}
