/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.util.Date;

/**
 * 
 * Base Class for Temporary Resources
 * 
 * @author Terry Packer
 */
public abstract class MangoRestTemporaryResourceModel {
	
	protected String id;
	protected Date expires;
	
	public MangoRestTemporaryResourceModel(){ }
	
	public MangoRestTemporaryResourceModel(String id){
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public Date getExpires(){
		return this.expires;
	}
	public void setExpires(Date expires){
		this.expires = expires;
	}

}
