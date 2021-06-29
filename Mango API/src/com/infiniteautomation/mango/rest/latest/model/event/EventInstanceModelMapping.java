/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapping;
import com.infiniteautomation.mango.rest.latest.model.comment.UserCommentModel;
import com.serotonin.m2m2.vo.event.EventInstanceI;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Terry Packer
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
        EventInstanceI evt = (EventInstanceI) from;
        AbstractEventTypeModel<?, ?, ?> eventTypeModel = mapper.map(evt.getEventType(), AbstractEventTypeModel.class, user);
        List<UserCommentModel> comments = evt.getEventComments().stream().map(UserCommentModel::new).collect(Collectors.toList());

        return new EventInstanceModel(
                evt.getId(),
                eventTypeModel,
                evt.getActiveTimestamp(),
                evt.getAcknowledgedByUserId(),
                evt.getAcknowledgedByUsername(),
                evt.getAcknowledgedTimestamp(),
                evt.getAlternateAckSource(),
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
