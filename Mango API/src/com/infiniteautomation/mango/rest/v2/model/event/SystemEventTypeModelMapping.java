/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.SystemEventType;

/**
 * @author Terry Packer
 *
 */
@Component
public class SystemEventTypeModelMapping implements RestModelMapping<SystemEventTypeModel, SystemEventType> {

    @Override
    public Class<SystemEventType> fromClass() {
        return SystemEventType.class;
    }
    
    @Override
    public Class<SystemEventTypeModel> toClass() {
        return SystemEventTypeModel.class;
    }

    @Override
    public SystemEventTypeModel map(Object from) {
        return new SystemEventTypeModel((SystemEventType) from);
    }

    @Override
    public String getTypeId() {
        return EventType.EventTypeNames.SYSTEM;
    }
    
    @Override
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventTypeModel.class));
    }
}
