/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.virtual.vo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.virtual.VirtualDataSourceDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractPollingDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class VirtualDataSourceModel extends AbstractPollingDataSourceModel<VirtualDataSourceVO>{

	public VirtualDataSourceModel(){
		super();
	}
	/**
	 * @param data
	 */
	public VirtualDataSourceModel(VirtualDataSourceVO data) {
		super(data);
	}
	
	@JsonGetter(value="polling")
	public boolean isPolling(){
		return this.data.isPolling();
	}
	
	@JsonSetter(value="polling")
	public void setPolling(boolean polling) {
		this.data.setPolling(polling);
	}

    @Override
    public String getModelType() {
        return VirtualDataSourceDefinition.TYPE_NAME;
    }
	
}
