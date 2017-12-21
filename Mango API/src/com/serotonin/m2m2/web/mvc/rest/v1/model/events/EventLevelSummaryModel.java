/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.events;

import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.UserEventLevelSummary;

/**
 * @author Terry Packer
 *
 */
public class EventLevelSummaryModel {

	private String level;
	private int unsilencedCount;
	private EventInstanceModel mostRecentUnsilenced;

	public EventLevelSummaryModel(){ }

	public EventLevelSummaryModel(UserEventLevelSummary summary) {
	    this.level = AlarmLevels.CODES.getCode(summary.getAlarmLevel());
	    this.unsilencedCount = summary.getUnsilencedCount();
	    if(summary.getLatest() != null)
	        this.mostRecentUnsilenced = new EventInstanceModel(summary.getLatest());
	}
	
	/**
	 * @param level
	 * @param count
	 * @param mostRecentActive
	 */
	public EventLevelSummaryModel(String level, int unsilencedCount,
			EventInstanceModel mostRecentUnsilenced) {
		super();
		this.level = level;
		this.unsilencedCount = unsilencedCount;
		this.mostRecentUnsilenced = mostRecentUnsilenced;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getUnsilencedCount() {
		return unsilencedCount;
	}

	public void setUnsilencedCount(int unsilencedCount) {
		this.unsilencedCount = unsilencedCount;
	}

	public EventInstanceModel getMostRecentUnsilenced() {
		return mostRecentUnsilenced;
	}

	public void setMostRecentUnsilenced(EventInstanceModel mostRecentUnsilenced) {
		this.mostRecentUnsilenced = mostRecentUnsilenced;
	}
	
}
