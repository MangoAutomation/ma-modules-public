/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.EventTypeMatcher;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

@Component
public class EventTypeMatcherMapping implements RestModelJacksonMapping<EventTypeMatcher, EventTypeMatcherModel> {

    @Override
    public Class<EventTypeMatcher> fromClass() {
        return EventTypeMatcher.class;
    }

    @Override
    public Class<EventTypeMatcherModel> toClass() {
        return EventTypeMatcherModel.class;
    }

    @Override
    public EventTypeMatcherModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new EventTypeMatcherModel((EventTypeMatcher) from);
    }

    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.AUDIT;
    }

}
