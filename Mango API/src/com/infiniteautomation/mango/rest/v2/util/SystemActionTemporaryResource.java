/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.util;

import java.util.Map;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.util.timeout.SystemActionTask;

/**
 * 
 * @author Terry Packer
 */
public class SystemActionTemporaryResource extends MangoRestTemporaryResource{

	private SystemActionTask task;
	
	/**
	 * @param resourceId
	 */
	public SystemActionTemporaryResource(String resourceId, SystemActionTask task) {
		super(resourceId);
		this.task = task;
		switch(this.task.getPriority()){
		case SystemActionTask.PRIORITY_HIGH:
			Common.backgroundProcessing.schedule(task);
			break;
		case SystemActionTask.PRIORITY_MEDIUM:
			Common.backgroundProcessing.executeMediumPriorityTask(task);
			break;
		}
	}

	
	public Map<String, Object> getResults(){
		return this.task.getResults();
	}
	
	public boolean isFinished() {
        return this.task.isFinished();
    }
	
	public void cancel(){
		this.task.cancel();
	}
}
