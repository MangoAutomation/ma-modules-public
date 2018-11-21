/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.MissingEventType;

/**
 * @author Terry Packer
 *
 */
@Component
public class MissingEventTypeModelMapping implements RestModelMapping<MissingEventTypeModel, MissingEventType> {

    @Override
    public Class<MissingEventType> fromClass() {
        return MissingEventType.class;
    }
    
    @Override
    public Class<MissingEventTypeModel> toClass() {
        return MissingEventTypeModel.class;
    }

    @Override
    public MissingEventTypeModel map(Object from) {
        return new MissingEventTypeModel((MissingEventType) from);
    }

    @Override
    public String getTypeId() {
        return EventType.EventTypeNames.MISSING;
    }
    
    @Override
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventTypeModel.class));
    }
}
