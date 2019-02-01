/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.mbus.rest;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.mbus.MBusDataSourceDefinition;
import com.serotonin.m2m2.mbus.MBusDataSourceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.dataSource.AbstractPollingDataSourceModel;

import net.sf.mbus4j.Connection;

/**
 * @author Terry Packer
 *
 */
public class MBusDataSourceModel extends AbstractPollingDataSourceModel<MBusDataSourceVO>{

	public MBusDataSourceModel(MBusDataSourceVO data) {
		super(data);
	}

	public MBusDataSourceModel() {
		super();
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return MBusDataSourceDefinition.DATA_SOURCE_TYPE;
	}
	
	@JsonGetter(value="connection")
	public Connection getConnection(){
	    return this.data.getConnection();
	}

	@JsonSetter(value="connection")
	public void setConnection(Connection connection){
	    this.data.setConnection(connection);
	}

}
