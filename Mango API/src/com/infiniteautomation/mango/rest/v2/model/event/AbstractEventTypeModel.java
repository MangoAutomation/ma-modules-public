/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.event.type.DuplicateHandling;
import com.serotonin.m2m2.rt.event.type.EventType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 * Read only models to send information out, not designed to create new
 * event types
 *
 * @author Terry Packer
 *
 */
@ApiModel(discriminator="eventType")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property="eventType")
public abstract class AbstractEventTypeModel<T extends EventType, SOURCE_ONE, SOURCE_TWO> {

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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected SOURCE_ONE reference1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected SOURCE_TWO reference2;

    public AbstractEventTypeModel(T type, SOURCE_ONE reference1, SOURCE_TWO reference2) {
        this(type);
        this.reference1 = reference1;
        this.reference2 = reference2;
    }

    public AbstractEventTypeModel(T type, SOURCE_ONE reference1) {
        this(type);
        this.reference1 = reference1;
    }

    public AbstractEventTypeModel(T type) {
        fromVO(type);
    }

    public AbstractEventTypeModel() {
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
     * @return the reference1
     */
    public SOURCE_ONE getReference1() {
        return reference1;
    }

    /**
     * @return the reference2
     */
    public SOURCE_TWO getReference2() {
        return reference2;
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

