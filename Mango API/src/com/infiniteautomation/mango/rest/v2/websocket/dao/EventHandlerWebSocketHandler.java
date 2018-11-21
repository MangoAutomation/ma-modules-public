/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.AbstractEventHandlerModel;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.EventHandlerService;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Jared Wiltshire
 */
@Component("EventHandlerWebSocketHandlerV2")
@WebSocketMapping("/v2/websocket/event-handlers")
public class EventHandlerWebSocketHandler extends DaoNotificationWebSocketHandler<AbstractEventHandlerVO<?>> {

    private final EventHandlerService service;
    private final RestModelMapper modelMapper;

    @Autowired
    public EventHandlerWebSocketHandler(EventHandlerService service, RestModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @Override
    protected boolean hasPermission(User user, AbstractEventHandlerVO<?> vo) {
        return service.hasReadPermission(user, vo);
    }

    @Override
    protected Object createModel(AbstractEventHandlerVO<?> vo) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object createModel(AbstractEventHandlerVO<?> vo, User user) {
        return modelMapper.map(vo, AbstractEventHandlerModel.class, user);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends AbstractEventHandlerVO<?>> event) {
        this.notify(event);
    }

    @Override
    protected boolean isModelPerUser() {
        return true;
    }
}
