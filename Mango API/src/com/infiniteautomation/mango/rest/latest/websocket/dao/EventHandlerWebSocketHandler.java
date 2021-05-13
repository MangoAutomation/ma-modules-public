/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.event.EventTypeMatcherModel;
import com.infiniteautomation.mango.rest.latest.model.event.handlers.AbstractEventHandlerModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.EventHandlerService;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Jared Wiltshire
 */
@Component("EventHandlerWebSocketHandlerV2")
@WebSocketMapping("/websocket/event-handlers")
public class EventHandlerWebSocketHandler<T extends AbstractEventHandlerVO> extends DaoNotificationWebSocketHandler<T> {

    private final EventHandlerService service;
    private final BiFunction<T, PermissionHolder, AbstractEventHandlerModel<T>> map;

    @Autowired
    public EventHandlerWebSocketHandler(EventHandlerService service, RestModelMapper modelMapper) {
        this.service = service;
        //Map the event types into the model
        this.map = (vo, user) -> {
            List<EventTypeMatcherModel> eventTypes = vo.getEventTypes().stream().map(type -> {
                return modelMapper.map(type, EventTypeMatcherModel.class, user);
            }).collect(Collectors.toList());
            @SuppressWarnings("unchecked")
            AbstractEventHandlerModel<T> model = modelMapper.map(vo, AbstractEventHandlerModel.class, user);
            model.setEventTypes(eventTypes);
            return model;
        };
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, T vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(T vo, PermissionHolder user) {
        return this.map.apply(vo, user);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends T> event) {
        this.notify(event);
    }
}
