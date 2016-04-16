/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.virtual.vo;

import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractPollingDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class VirtualDataSourceModel extends AbstractPollingDataSourceModel<VirtualDataSourceVO>{

	public VirtualDataSourceModel(){
		super(new VirtualDataSourceVO());
	}
	/**
	 * @param data
	 */
	public VirtualDataSourceModel(VirtualDataSourceVO data) {
		super(data);
	}
}
