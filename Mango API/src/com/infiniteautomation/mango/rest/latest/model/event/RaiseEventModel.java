/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import java.util.Map;

import com.serotonin.m2m2.rt.event.AlarmLevels;

/**
 *
 *
 * @author Terry Packer
 */
public class RaiseEventModel {
    private AbstractEventTypeModel<?,?,?> event;
    private AlarmLevels level;
    private String message;
    private Map<String, Object> context;

    /**
     * @return the event
     */
    public AbstractEventTypeModel<?,?,?> getEvent() {
        return event;
    }
    /**
     * @param event the event to set
     */
    public void setEvent(AbstractEventTypeModel<?,?,?> event) {
        this.event = event;
    }
    /**
     * @return the level
     */
    public AlarmLevels getLevel() {
        return level;
    }
    /**
     * @param level the level to set
     */
    public void setLevel(AlarmLevels level) {
        this.level = level;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * @return the context
     */
    public Map<String, Object> getContext() {
        return context;
    }
    /**
     * @param context the context to set
     */
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

}
