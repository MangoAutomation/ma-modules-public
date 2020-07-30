/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.PublisherEventType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class PublisherEventTypeModelMapping implements RestModelJacksonMapping<PublisherEventType, PublisherEventTypeModel> {

    @Override
    public Class<PublisherEventType> fromClass() {
        return PublisherEventType.class;
    }

    @Override
    public Class<PublisherEventTypeModel> toClass() {
        return PublisherEventTypeModel.class;
    }

    @Override
    public PublisherEventTypeModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new PublisherEventTypeModel((PublisherEventType) from);
    }
    
    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.PUBLISHER;
    }
}
