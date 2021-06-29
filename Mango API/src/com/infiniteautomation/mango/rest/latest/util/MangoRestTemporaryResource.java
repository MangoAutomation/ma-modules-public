/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.util;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;

/**
 * Temporary Resources potentially expire at some point in the future.
 * 
 * @author Terry Packer
 */
public abstract class MangoRestTemporaryResource<T extends MangoRestTemporaryResource<?>> extends TimeoutClient{
	
	protected final String resourceId;
	protected Date expiration;
	@JsonIgnore
	private final MangoRestTemporaryResourceContainer<T> container;
	@JsonIgnore
	private TimeoutTask task;


	/**
	 * Create the resource and plan to schedule it's timeout later
	 * @param resourceId
	 * @param container
	 */
	@SuppressWarnings("unchecked")
	public MangoRestTemporaryResource(String resourceId, MangoRestTemporaryResourceContainer<T> container){
		this.resourceId = resourceId;
		this.container = container;
		this.container.put(resourceId, (T) this);
	}

	/**
	 * Create and schedule the resource to expire
	 * @param resourceId
	 * @param expiration
	 * @param container
	 */
	@SuppressWarnings("unchecked")
	public MangoRestTemporaryResource(String resourceId, MangoRestTemporaryResourceContainer<T> container, Date expiration){
		this.resourceId = resourceId;
		this.expiration = expiration;
		this.container = container;
		this.container.put(resourceId, (T)this);
		this.task = new TimeoutTask(expiration, this);
	}
	
	/**
	 * Schedule a timeout for the resource
	 * @param expiration
	 * @param container
	 */
	public void schedule(Date expiration){
		this.expiration = expiration;
		this.cancelTimeout();
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
		if(this.expiration != null)
			return this.expiration.getTime();
		else
			return -1;
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
