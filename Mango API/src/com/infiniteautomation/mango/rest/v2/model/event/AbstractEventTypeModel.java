/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.type.EventType;

/**
 * 
 * Read only models to send information out, not designed to create new 
 * event types
 * 
 * @author Terry Packer
 *
 */
public abstract class AbstractEventTypeModel<T extends EventType> {
    
    private String eventType;
    private String subType;
    private String duplicateHandling;
    private Integer referenceId1;
    private Integer referenceId2;
    private Boolean rateLimited;
    
    
    public AbstractEventTypeModel(T type) {
        this.eventType = type.getEventType();
        this.subType = type.getEventSubtype();
        this.duplicateHandling = EventType.DUPLICATE_HANDLING_CODES.getCode(type.getDuplicateHandling());
        this.referenceId1 = type.getReferenceId1();
        this.referenceId2 = type.getReferenceId2();
        this.rateLimited = type.isRateLimited();
    }


    /**
     * @return the eventType
     */
    public String getEventType() {
        return eventType;
    }


    /**
     * @return the subType
     */
    public String getSubType() {
        return subType;
    }

    /**
     * @return the duplicateHandling
     */
    public String getDuplicateHandling() {
        return duplicateHandling;
    }


    /**
     * @return the referenceId1
     */
    public Integer getReferenceId1() {
        return referenceId1;
    }


    /**
     * @return the referenceId2
     */
    public Integer getReferenceId2() {
        return referenceId2;
    }


    /**
     * @return the rateLimited
     */
    public Boolean getRateLimited() {
        return rateLimited;
    }
    
}
