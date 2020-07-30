/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.infiniteautomation.mango.rest.v2.model.comment.UserCommentModel;
import com.serotonin.m2m2.vo.event.EventInstanceI;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class EventInstanceModelMapping implements RestModelMapping<EventInstanceI, EventInstanceModel> {

    @Override
    public Class<? extends EventInstanceI> fromClass() {
        return EventInstanceI.class;
    }

    @Override
    public Class<? extends EventInstanceModel> toClass() {
        return EventInstanceModel.class;
    }

    @Override
    public EventInstanceModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        EventInstanceI evt = (EventInstanceI)from;
        AbstractEventTypeModel<?,?,?> eventTypeModel = mapper.map(evt.getEventType(), AbstractEventTypeModel.class, user);
        List<UserCommentModel> comments = evt.getEventComments().stream().map(c -> new UserCommentModel(c)).collect(Collectors.toList());
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
                evt.getMessage(),
                evt.getRtnMessage(),
                comments
                );
    }
}
