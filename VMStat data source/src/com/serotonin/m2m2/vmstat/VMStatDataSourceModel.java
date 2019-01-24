/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.vmstat;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractDataSourceModel;

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
		super();
	}

	
	@JsonGetter(value="pollSeconds")
	public int getPollSeconds(){
	    return this.data.getPollSeconds();
	}

	@JsonSetter(value="pollSeconds")
	public void setPollSeconds(int pollSeconds){ //Cannot change from seconds
	    this.data.setPollSeconds(pollSeconds);
	}

	@JsonGetter("outputScale")
	public String getOutputScale() {
	    return VMStatDataSourceVO.OUTPUT_SCALE_CODES.getCode(this.data.getOutputScale());
	}

	@JsonSetter("outputScale")
	public void setOutputScale(String outputScale) {
	    this.data.setOutputScale(VMStatDataSourceVO.OUTPUT_SCALE_CODES.getId(outputScale));
	}

    @Override
    public String getModelType() {
        return VMStatDataSourceDefinition.DATA_SOURCE_TYPE;
    }



}
