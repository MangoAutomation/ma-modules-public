/*
 * Copyright (C) 2023 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.serotonin.m2m2.vo.event.EventInstanceI;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Mert Cingöz
 */
@Component
public class EventInstanceReducedModelMapping implements RestModelMapping<EventInstanceI, EventInstanceReducedModel> {

    @Override
    public Class<? extends EventInstanceI> fromClass() {
        return EventInstanceI.class;
    }

    @Override
    public Class<? extends EventInstanceReducedModel> toClass() {
        return EventInstanceReducedModel.class;
    }

    @Override
    public EventInstanceReducedModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        EventInstanceI evt = (EventInstanceI) from;
        AbstractEventTypeModel<?, ?, ?> eventTypeModel = mapper.map(evt.getEventType(), AbstractEventTypeModel.class, user);
        return new EventInstanceReducedModel(evt, eventTypeModel);
    }
}
