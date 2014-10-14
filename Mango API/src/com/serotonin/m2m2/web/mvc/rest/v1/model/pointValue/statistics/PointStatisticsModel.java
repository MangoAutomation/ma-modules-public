/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

/**
 * @author Terry Packer
 *
 */
public class PointStatisticsModel {
	
	@JsonProperty
	private PointValueTimeModel startPoint;
	@JsonProperty
	private PointValueTimeModel endPoint;
	@JsonProperty
	private boolean hasData = false; //Does this object have any data
	
	@JsonGetter("message")
	public String getMessage(){
		return "no data";
	}
	
	public PointValueTimeModel getStartPoint() {
		return startPoint;
	}
	public void setStartPoint(PointValueTimeModel startPoint) {
		this.startPoint = startPoint;
	}
	public PointValueTimeModel getEndPoint() {
		return endPoint;
	}
	public void setEndPoint(PointValueTimeModel endPoint) {
		this.endPoint = endPoint;
	}

	public boolean isHasData() {
		return hasData;
	}

	public void setHasData(boolean hasData) {
		this.hasData = hasData;
	}


	
	
	
	
	
	
}
