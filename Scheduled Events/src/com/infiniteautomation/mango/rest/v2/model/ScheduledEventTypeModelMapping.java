/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.serotonin.m2m2.scheduledEvents.ScheduledEventType;

/**
 * @author Terry Packer
 *
 */
@Component
public class ScheduledEventTypeModelMapping implements RestModelMapping<ScheduledEventType, ScheduledEventTypeModel>{

    @Override
    public ScheduledEventTypeModel map(Object o) {
        return new ScheduledEventTypeModel((ScheduledEventType)o);
    }

    @Override
    public Class<ScheduledEventTypeModel> toClass() {
        return ScheduledEventTypeModel.class;
    }

    @Override
    public Class<ScheduledEventType> fromClass() {
        return ScheduledEventType.class;
    }

    @Override
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventTypeModel.class));
    }
    
}
