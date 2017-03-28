/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 * @author Terry Packer
 *
 */
public class WatchListModel extends WatchListSummaryModel{

	private List<WatchListDataPointModel> points;

	public WatchListModel(){
		super();
		this.points = new ArrayList<WatchListDataPointModel>();
	}
	
	public WatchListModel(WatchListVO data, List<WatchListDataPointModel> points){
		super(data);
		this.points = points;
	}
	
	@JsonGetter("points")
	public List<WatchListDataPointModel> getPoints(){
		return this.points;
	}
	@JsonSetter("points")
	public void setPoints(List<WatchListDataPointModel> points){
		this.points = points;
	}
}
