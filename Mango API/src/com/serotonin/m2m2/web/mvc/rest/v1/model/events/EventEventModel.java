/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.events;

import com.serotonin.m2m2.rt.event.EventInstance;

/**
 * @author Terry Packer
 *
 */
public class EventEventModel {

	private EventEventTypeEnum type;
	private EventInstanceModel event;
	
	public EventEventModel(){ }

	public EventEventModel(EventEventTypeEnum type, EventInstance evt){
		this.type = type;
		this.event = new EventInstanceModel(evt);
	}

	public EventEventTypeEnum getType() {
		return type;
	}

	public void setType(EventEventTypeEnum type) {
		this.type = type;
	}

	public EventInstanceModel getEvent() {
		return event;
	}

	public void setEvent(EventInstanceModel event) {
		this.event = event;
	}
	
	
}
