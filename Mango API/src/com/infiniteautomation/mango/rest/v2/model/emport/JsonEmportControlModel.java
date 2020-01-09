/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.emport;

/**
 * Simple container for controlling 
 * running Json Emports
 * 
 * @author Terry Packer
 */
public class JsonEmportControlModel {
	
	//ID of the import
	private String resourceId;
	//Should we cancel it
	private boolean cancel;
	
	public JsonEmportControlModel(){}

	public JsonEmportControlModel(String resourceId, boolean cancel) {
		super();
		this.resourceId = resourceId;
		this.cancel = cancel;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	public boolean isCancel() {
		return cancel;
	}
	
	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}
	
	

}
