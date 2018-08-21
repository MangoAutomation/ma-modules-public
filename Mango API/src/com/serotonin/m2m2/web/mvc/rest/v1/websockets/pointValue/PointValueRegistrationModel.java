/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets.pointValue;

import java.util.EnumSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public class PointValueRegistrationModel {

	@JsonProperty("xid")
	private String dataPointXid; //Data point to register against
	
	@JsonProperty("eventTypes")
	private Set<PointValueEventType> eventTypes; //Events to listen for

	
	public PointValueRegistrationModel(){
	}

	public String getDataPointXid() {
		return dataPointXid;
	}

	public void setDataPointXid(String dataPointXid) {
		this.dataPointXid = dataPointXid;
	}

	public Set<PointValueEventType> getEventTypes() {
		return eventTypes == null ? EnumSet.noneOf(PointValueEventType.class) : eventTypes;
	}

	public void setEventTypes(Set<PointValueEventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

}
