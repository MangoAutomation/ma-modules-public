/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author Terry Packer
 *
 */
public class ValueChangeStatisticsModel extends PointStatisticsModel{
	
	@JsonProperty
	private int changes;

	@Override
	@JsonGetter("message")
	public String getMessage(){
		return "Value Change Statistics";
	}
	
	public int getChanges() {
		return changes;
	}

	public void setChanges(int changes) {
		this.changes = changes;
	}
	
	
}
