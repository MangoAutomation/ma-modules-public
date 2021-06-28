/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
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
    private final RestModelMapper modelMapper;

    @Autowired
    public EventHandlerWebSocketHandler(EventHandlerService service, RestModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, T vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(T vo, ApplicationEvent event, PermissionHolder user) {
        return modelMapper.map(vo, AbstractEventHandlerModel.class, user);
    }

    @SuppressWarnings("SpringEventListenerInspection")
    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends T> event) {
        this.notify(event);
    }
}
