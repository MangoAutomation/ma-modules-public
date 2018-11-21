/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.PublisherEventType;

/**
 * @author Terry Packer
 *
 */
@Component
public class PublisherEventTypeModelMapping implements RestModelMapping<PublisherEventType, PublisherEventTypeModel> {

    @Override
    public Class<PublisherEventType> fromClass() {
        return PublisherEventType.class;
    }
    
    @Override
    public Class<PublisherEventTypeModel> toClass() {
        return PublisherEventTypeModel.class;
    }

    @Override
    public PublisherEventTypeModel map(Object from) {
        return new PublisherEventTypeModel((PublisherEventType) from);
    }

    @Override
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventTypeModel.class));
    }
}
