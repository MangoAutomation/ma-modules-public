/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.rt.event.type.EventType;

/**
 * For transporting event types without the source objects
 * @author Terry Packer
 */
public abstract class AbstractEventTypeWithoutSourcesModel<T extends EventType> extends BaseEventTypeModel<T> {

    public AbstractEventTypeWithoutSourcesModel() { }

    public AbstractEventTypeWithoutSourcesModel(T type) {
        fromVO(type);
    }

}
