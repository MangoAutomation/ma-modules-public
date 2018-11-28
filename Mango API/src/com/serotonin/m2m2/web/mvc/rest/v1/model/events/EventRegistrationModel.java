/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.events;

import java.util.Set;

import com.serotonin.m2m2.rt.event.AlarmLevels;

/**
 * @author Terry Packer
 *
 */
public class EventRegistrationModel {

    private Set<EventEventTypeEnum> eventTypes;
    private Set<AlarmLevels> levels;

    public Set<AlarmLevels> getLevels() {
        return levels;
    }

    public void setLevels(Set<AlarmLevels> levels) {
        this.levels = levels;
    }

    public Set<EventEventTypeEnum> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(Set<EventEventTypeEnum> eventTypes) {
        this.eventTypes = eventTypes;
    }

}
