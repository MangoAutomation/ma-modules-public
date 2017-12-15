/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import java.util.Map;

import com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel;

/**
 * 
 *
 * @author Terry Packer
 */
public class RaiseEventModel {
    private EventTypeModel event;
    private String level;
    private String message;
    private Map<String, Object> context;

    /**
     * @return the event
     */
    public EventTypeModel getEvent() {
        return event;
    }
    /**
     * @param event the event to set
     */
    public void setEvent(EventTypeModel event) {
        this.event = event;
    }
    /**
     * @return the level
     */
    public String getLevel() {
        return level;
    }
    /**
     * @param level the level to set
     */
    public void setLevel(String level) {
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
