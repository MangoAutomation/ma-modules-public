/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public class StartsAndRuntimeListModel extends PointStatisticsModel{

	@Override
	@JsonGetter("message")
	public String getMessage(){
		return "Starts and Runtimes Statistics";
	}
	
	@JsonProperty
	private List<StartsAndRuntimeModel> startsAndRuntime;

	public List<StartsAndRuntimeModel> getStartsAndRuntime() {
		return startsAndRuntime;
	}
	public void setStartsAndRuntime(List<StartsAndRuntimeModel> startsAndRuntime) {
		this.startsAndRuntime = startsAndRuntime;
	}
}
