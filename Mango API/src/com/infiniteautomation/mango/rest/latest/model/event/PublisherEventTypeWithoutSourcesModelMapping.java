/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.PublisherEventType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class PublisherEventTypeWithoutSourcesModelMapping implements RestModelJacksonMapping<PublisherEventType, PublisherEventTypeWithoutSourcesModel> {

    @Override
    public Class<PublisherEventType> fromClass() {
        return PublisherEventType.class;
    }

    @Override
    public Class<PublisherEventTypeWithoutSourcesModel> toClass() {
        return PublisherEventTypeWithoutSourcesModel.class;
    }

    @Override
    public PublisherEventTypeWithoutSourcesModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new PublisherEventTypeWithoutSourcesModel((PublisherEventType) from);
    }

    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.PUBLISHER;
    }
}
