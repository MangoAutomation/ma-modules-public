/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.event.type.DuplicateHandling;
import com.serotonin.m2m2.rt.event.type.EventType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This is a base class to hold common fields
 * but retain different hierarchy for model mappings
 * @author Terry Packer
 */
@ApiModel(discriminator="eventType")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property="eventType")
public abstract class BaseEventTypeModel<T extends EventType> {

    @ApiModelProperty("Type of event")
    protected String eventType;

    @ApiModelProperty("Sub-type of event")
    protected String subType;

    protected DuplicateHandling duplicateHandling;

    @ApiModelProperty("ID used in event type/subtype combination")
    protected Integer referenceId1;

    @ApiModelProperty("ID used in event type/subtype combination")
    protected Integer referenceId2;

    @ApiModelProperty("Is the alarm rate limited")
    protected Boolean rateLimited;

    public BaseEventTypeModel() { }

    public BaseEventTypeModel(T type) {
        fromVO(type);
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
    public DuplicateHandling getDuplicateHandling() {
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

    /**
     * EventType(s) are lacking setters
     *  so they must be created/filled in this method
     *  implemented in the concrete model class
     * @return
     */
    public abstract T toVO();

    public void fromVO(T type) {
        this.eventType = type.getEventType();
        this.subType = type.getEventSubtype();
        this.duplicateHandling = type.getDuplicateHandling();
        this.referenceId1 = type.getReferenceId1() == Common.NEW_ID ? null : type.getReferenceId1();
        this.referenceId2 = type.getReferenceId2() == Common.NEW_ID ? null : type.getReferenceId2();
        this.rateLimited = type.isRateLimited();
    }

}
