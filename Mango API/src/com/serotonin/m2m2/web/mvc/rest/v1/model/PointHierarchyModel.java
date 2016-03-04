/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.vo.DataPointSummary;
import com.serotonin.m2m2.vo.hierarchy.PointFolder;

/**
 * @author Terry Packer
 *
 */
public class PointHierarchyModel extends AbstractRestModel<PointFolder>{

	//Helper to speed up outgoing models
	private Map<Integer, String> dataSourceXidMap;
	
	public PointHierarchyModel(){
		super(new PointFolder());
		this.data.setSubfolders(new ArrayList<PointFolder>());
		this.data.setPoints(new ArrayList<DataPointSummary>());
	}
	
	/**
	 * @param data
	 */
	public PointHierarchyModel(PointFolder data, Map<Integer, String> dataSourceXidMap) {
		super(data);
		this.dataSourceXidMap = dataSourceXidMap;
	}
	
	/*
	 * Since PointFolders do not yet have an XID
	 * this means we must use IDs to describe them
	 * 
	 */
	@JsonGetter("id")
	public int getId(){
		return this.data.getId();
	}
	@JsonSetter("id")
	public void setId(int id){
		this.data.setId(id);
	}
	
	@JsonGetter("name")
	public String getName(){
		return this.data.getName();
	}
	@JsonSetter("name")
	public void setName(String name){
		this.data.setName(name);
	}
	
	@JsonGetter("subfolders")
	public List<PointHierarchyModel> getSubfolders(){
		List<PointHierarchyModel> subfolders = new ArrayList<PointHierarchyModel>(this.data.getSubfolders().size());
		for(PointFolder folder : this.data.getSubfolders()){
			subfolders.add(new PointHierarchyModel(folder, this.dataSourceXidMap));
		}
		return subfolders;
	}
	@JsonSetter("subfolders")
	public void setSubfolders(List<PointHierarchyModel> subfolders){
		for(PointHierarchyModel subfolder : subfolders){
			this.data.addSubfolder(subfolder.getData());
		}
	}
	
	@JsonGetter("points")
	public List<DataPointSummaryModel> getPoints(){
		List<DataPointSummaryModel> points = new ArrayList<DataPointSummaryModel>();
		for(DataPointSummary summary : this.data.getPoints())
			points.add(new DataPointSummaryModel(summary, this.dataSourceXidMap.get(summary.getDataSourceId())));
		
		return points;
	}
	@JsonSetter("points")
	public void setPoints(List<DataPointSummaryModel> points){
		for(DataPointSummaryModel model : points){
			this.data.addDataPoint(model.getData());
		}
	}


}
