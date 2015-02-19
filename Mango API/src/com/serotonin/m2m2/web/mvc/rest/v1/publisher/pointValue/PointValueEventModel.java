/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.publisher.pointValue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeModel;

/**
 * @author Terry Packer
 *
 */
public class PointValueEventModel {

	@JsonProperty("event")
	private PointValueEventType event;
	
	@JsonProperty("value")
	private PointValueTimeModel value;
	
	@JsonProperty("xid")
	private String xid;
	
	public PointValueEventModel(String xid, PointValueEventType type, PointValueTimeModel model){
		this.xid = xid;
		this.event = type;
		this.value = model;
	}

	public PointValueEventModel(){
		
	}
	
	public PointValueEventType getEvent() {
		return event;
	}

	public void setEvent(PointValueEventType event) {
		this.event = event;
	}

	public PointValueTimeModel getValue() {
		return value;
	}

	public void setValue(PointValueTimeModel value) {
		this.value = value;
	}
	
	public String getXid(){
		return this.xid;
	}
	
	public void setXid(String xid){
		this.xid = xid;
	}
	
}
