/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.util;

import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;
import com.serotonin.m2m2.web.mvc.rest.v1.model.MangoRestTemporaryResourceModel;

/**
 * 
 * @author Terry Packer
 */
public abstract class MangoRestTemporaryResource extends TimeoutClient{
	
	protected final String resourceId;
	protected long expiration = 0;
	private MangoRestTemporaryResourceContainer<? extends MangoRestTemporaryResource> container;
	private TimeoutTask task;

	
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
	 * Create a model
	 * @return
	 */
	protected abstract MangoRestTemporaryResourceModel createModel();
	
	/**
	 * Get the model for the resource
	 * @return
	 */
	public MangoRestTemporaryResourceModel getModel(){
		MangoRestTemporaryResourceModel model = createModel();
		if(this.expiration >= 0)
			model.setExpires(expiration);
		return model;
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
