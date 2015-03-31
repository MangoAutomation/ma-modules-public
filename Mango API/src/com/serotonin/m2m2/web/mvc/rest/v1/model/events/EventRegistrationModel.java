/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.events;

import java.util.List;

/**
 * @author Terry Packer
 *
 */
public class EventRegistrationModel {

	private List<EventEventTypeEnum> eventTypes;
	private List<String> levels;

	public List<String> getLevels() {
		return levels;
	}

	public void setLevels(List<String> levels) {
		this.levels = levels;
	}

	public List<EventEventTypeEnum> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<EventEventTypeEnum> eventTypes) {
		this.eventTypes = eventTypes;
	}
	
}
