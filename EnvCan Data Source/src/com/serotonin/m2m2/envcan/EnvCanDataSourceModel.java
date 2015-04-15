/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.envcan;

<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
=======
>>>>>>> branch 'development' of https://github.com/infiniteautomation/ma-modules-public.git
import com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractDataSourceModel;

/**
 * @author Terry Packer
 *
 */
public class EnvCanDataSourceModel extends AbstractDataSourceModel<EnvCanDataSourceVO>{

	private EnvCanDataSourceVO data;
	
	public EnvCanDataSourceModel() {
		super(new EnvCanDataSourceVO());
	}
	
	/**
	 * @param data
	 */
	public EnvCanDataSourceModel(EnvCanDataSourceVO data) {
		super(data);
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		// TODO Implement when we have a model, should be the TYPE_NAME in the Model Definition
		return null;
	}
	
	@JsonGetter("stationId")
	public int getStationId() {
	    return this.data.getStationId();
	}

	@JsonSetter("stationId")
	public void setStationId(int stationId) {
	    this.data.setStationId(stationId);
	}



	
}
