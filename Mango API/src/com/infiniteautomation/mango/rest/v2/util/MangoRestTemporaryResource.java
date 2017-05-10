/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;

/**
 * Temporary Resources potentially expire at some point in the future.
 * 
 * @author Terry Packer
 */
public abstract class MangoRestTemporaryResource extends TimeoutClient{
	
	protected final String resourceId;
	protected long expiration = 0;
	@JsonIgnore
	private MangoRestTemporaryResourceContainer<? extends MangoRestTemporaryResource> container;
	@JsonIgnore
	private TimeoutTask task;

	/**
	 * 
	 * @param resourceId
	 */
	public MangoRestTemporaryResource(String resourceId){
		this.resourceId = resourceId;
	}

	/**
	 * Schedule a timeout for the resource
	 * @param expiration
	 * @param container
	 */
	public void schedule(long expiration, MangoRestTemporaryResourceContainer<? extends MangoRestTemporaryResource> container){
		this.expiration = expiration;
		this.container = container;
		if(this.task != null)
			this.task.cancel();
		this.task = new TimeoutTask(expiration, this);
	}
	
	/**
	 * Cancel the timeout task
	 */
	public void cancelTimeout(){
		if(this.task != null)
			this.task.cancel();
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.util.timeout.TimeoutClient#scheduleTimeout(long)
	 */
	@Override
	public void scheduleTimeout(long fireTime) {
		this.container.remove(this.resourceId);
	}
	
	/**
	 * ID for this resource
	 * @return
	 */
	public String getResourceId(){
		return this.resourceId;
	}

	/**
	 * Timestamp at which the resource will expire
	 * @return
	 */
	public long getExpires(){
		return this.expiration;
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.rt.maint.work.WorkItem#getTaskId()
	 */
	@Override
	public String getTaskId() {
		return "TR_" + this.resourceId;
	}


	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.util.timeout.TimeoutClient#getThreadName()
	 */
	@Override
	public String getThreadName() {
		return "Temporary Resource Timeout for : " + this.resourceId;
	}
}
