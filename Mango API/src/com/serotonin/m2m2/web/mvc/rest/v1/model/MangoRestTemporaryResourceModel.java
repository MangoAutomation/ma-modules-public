/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

/**
 * 
 * Base Class for Temporary Resources
 * 
 * @author Terry Packer
 */
public abstract class MangoRestTemporaryResourceModel {
	
	protected String id;
	protected long expires;
	
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
	
	public long getExpires(){
		return this.expires;
	}
	public void setExpires(long expires){
		this.expires = expires;
	}

}
