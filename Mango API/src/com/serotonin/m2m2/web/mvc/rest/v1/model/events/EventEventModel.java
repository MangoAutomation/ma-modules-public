/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.events;

import java.util.Date;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.rt.event.EventInstance;

/**
 * @author Terry Packer
 *
 */
public class EventEventModel {

	private EventEventTypeEnum type;
	private int id;
	private String level;
	private Date activeTime;
	private String message;
	private boolean acknowledged;
	
	public EventEventModel(){ }

	public EventEventModel(EventEventTypeEnum type, EventInstance evt){
		this.type = type;
		
		this.id = evt.getId();
		this.level = AlarmLevels.CODES.getCode(evt.getAlarmLevel());
		this.activeTime = new Date(evt.getActiveTimestamp());
		if(evt.getMessage() != null)
			this.message = evt.getMessage().translate(Common.getTranslations());
		this.acknowledged = evt.isAcknowledged();
		
	}
	
	/**
	 * @param type
	 * @param id
	 * @param level
	 * @param activeTimestamp
	 * @param message
	 * @param acknowledged
	 */
	public EventEventModel(EventEventTypeEnum type, int id, String level,
			Date activeTime, String message, boolean acknowledged) {
		super();
		this.type = type;
		this.id = id;
		this.level = level;
		this.activeTime = activeTime;
		this.message = message;
		this.acknowledged = acknowledged;
	}

	public EventEventTypeEnum getType() {
		return type;
	}

	public void setType(EventEventTypeEnum type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public Date getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(Date activeTimestamp) {
		this.activeTime = activeTimestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isAcknowledged() {
		return acknowledged;
	}

	public void setAcknowledged(boolean acknowledged) {
		this.acknowledged = acknowledged;
	}
	
	
}
