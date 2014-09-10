/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public class PointValueRegistrationModel {

	@JsonProperty("xid")
	private String dataPointXid; //Data point to register against
	
	@JsonProperty("eventTypes")
	private List<PointValueEventType> eventTypes; //Events to listen for

	
	public PointValueRegistrationModel(){
		this.eventTypes = new ArrayList<PointValueEventType>();
	}

	public String getDataPointXid() {
		return dataPointXid;
	}

	public void setDataPointXid(String dataPointXid) {
		this.dataPointXid = dataPointXid;
	}

	public List<PointValueEventType> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<PointValueEventType> eventTypes) {
		this.eventTypes = eventTypes;
	}
	
	
}
