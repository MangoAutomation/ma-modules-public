/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.serotonin.m2m2.watchlist.WatchListSummaryModelDefinition;
import com.serotonin.m2m2.watchlist.WatchListVO;

/**
 * Summary of a watchlist without its points
 * basically a subset of the watchlist members
 * 
 * @author Terry Packer
 *
 */
public class WatchListSummaryModel extends AbstractVoModel<WatchListVO>{
	
	public WatchListSummaryModel(){
		super(new WatchListVO());
	}
	
	public WatchListSummaryModel(WatchListVO data) {
		super(data);
	}

	@JsonGetter("username")
	public String getUsername(){
		return this.data.getUsername();
	}
	@JsonSetter("username")
	public void setUsername(String username){
		this.data.setUsername(username);
	}

	@JsonGetter("readPermission")
	public String getReadPermission(){
		return this.data.getReadPermission();
	}
	@JsonSetter("readPermission")
	public void setReadPermission(String readPermission){
		this.data.setReadPermission(readPermission);
	}
	
	@JsonGetter("editPermission")
	public String getEditPermission(){
		return this.data.getEditPermission();
	}
	@JsonSetter("editPermission")
	public void setEditPermission(String editPermission){
		this.data.setEditPermission(editPermission);
	}
	
	@JsonGetter("type")
	public String getType(){
		return this.data.getType();
	}
	@JsonSetter("type")
	public void setType(String type){
		this.data.setType(type);
	}

	@JsonGetter("query")
	public String getQuery(){
		return this.data.getQuery();
	}
	@JsonSetter("query")
	public void setQuery(String query){
		this.data.setEditPermission(query);
	}

	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.model.AbstractVoModel#getModelType()
	 */
	@Override
	public String getModelType() {
		return WatchListSummaryModelDefinition.TYPE_NAME;
	}
}
