/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.serotonin.m2m2.rt.event.type.EventType;

/**
 *
 * Read only models to send information out, not designed to create new
 * event types
 *
 * @author Terry Packer
 *
 */
public abstract class AbstractEventTypeModel<T extends EventType, SOURCE_ONE, SOURCE_TWO> extends BaseEventTypeModel<T>{

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
        super(type);
    }

    public AbstractEventTypeModel() {
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

}

