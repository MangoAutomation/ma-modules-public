/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.infiniteautomation.mango.rest.v2.mapping.RestModelTypeIdResolver;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.type.EventType;

/**
 * 
 * 
 * @author Terry Packer
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property="eventType")
@JsonTypeIdResolver(RestModelTypeIdResolver.class)
public abstract class AbstractEventTypeModel {
    
    public AbstractEventTypeModel(EventType type) {
        this.eventType = type.getEventType();
        this.subType = type.getEventSubtype();
        //TODO When we consolidate EventTypeVO this.description = type.getDescription();
        this.duplicateHandling = EventType.DUPLICATE_HANDLING_CODES.getCode(type.getDuplicateHandling());
        this.referenceId1 = type.getReferenceId1();
        this.referenceId2 = type.getReferenceId2();
        this.rateLimited = type.isRateLimited();
    }
    
    private String eventType;
    private String subType;
    private TranslatableMessage description;
    private String duplicateHandling;
    private Integer referenceId1;
    private Integer referenceId2;
    private Boolean rateLimited;
    
    /**
     * @return the eventType
     */
    public String getEventType() {
        return eventType;
    }
    /**
     * @param eventType the eventType to set
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    /**
     * @return the subType
     */
    public String getSubType() {
        return subType;
    }
    /**
     * @param subType the subType to set
     */
    public void setSubType(String subType) {
        this.subType = subType;
    }
    /**
     * @return the description
     */
    public TranslatableMessage getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(TranslatableMessage description) {
        this.description = description;
    }
    /**
     * @return the duplicateHandling
     */
    public String getDuplicateHandling() {
        return duplicateHandling;
    }
    /**
     * @param duplicateHandling the duplicateHandling to set
     */
    public void setDuplicateHandling(String duplicateHandling) {
        this.duplicateHandling = duplicateHandling;
    }
    /**
     * @return the referenceId1
     */
    public Integer getReferenceId1() {
        return referenceId1;
    }
    /**
     * @param referenceId1 the referenceId1 to set
     */
    public void setReferenceId1(Integer referenceId1) {
        this.referenceId1 = referenceId1;
    }
    /**
     * @return the referenceId2
     */
    public Integer getReferenceId2() {
        return referenceId2;
    }
    /**
     * @param referenceId2 the referenceId2 to set
     */
    public void setReferenceId2(Integer referenceId2) {
        this.referenceId2 = referenceId2;
    }
    /**
     * @return the rateLimited
     */
    public Boolean getRateLimited() {
        return rateLimited;
    }
    /**
     * @param rateLimited the rateLimited to set
     */
    public void setRateLimited(Boolean rateLimited) {
        this.rateLimited = rateLimited;
    }
}
