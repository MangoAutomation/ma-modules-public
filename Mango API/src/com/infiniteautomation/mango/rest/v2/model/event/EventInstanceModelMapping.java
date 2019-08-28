/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class EventInstanceModelMapping implements RestModelMapping<EventInstance, EventInstanceModel>{

    @Override
    public Class<? extends EventInstance> fromClass() {
        return EventInstance.class;
    }

    @Override
    public Class<? extends EventInstanceModel> toClass() {
        return EventInstanceModel.class;
    }

    @Override
    public EventInstanceModel map(Object from, User user, RestModelMapper mapper) {
        EventInstance evt = (EventInstance)from;
        AbstractEventTypeModel<?,?,?> eventTypeModel = mapper.map(evt.getEventType(), AbstractEventTypeModel.class, user); 
        return new EventInstanceModel(
                evt.getId(),
                eventTypeModel,
                evt.getActiveTimestamp(),
                evt.getAcknowledgedByUserId(),
                evt.getAcknowledgedByUsername(),
                evt.getAcknowledgedTimestamp(),
                evt.isRtnApplicable(),
                evt.getRtnTimestamp(),
                evt.getRtnCause(),
                evt.getAlarmLevel(),
                evt.getMessage()
                );
    }

}
