/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.event.EventInstanceModel;
import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.db.EventInstanceTableDefinition;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.EventInstanceVO;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/event-instances")
public class EventInstanceWebSocketHandler extends DaoNotificationWebSocketHandler<EventInstanceVO, EventInstanceTableDefinition> {

    private final BiFunction<EventInstanceVO, User, EventInstanceModel> map;

    @Autowired
    public EventInstanceWebSocketHandler(RestModelMapper modelMapper) {
        this.map = (vo, user) -> {
            return modelMapper.map(vo, EventInstanceModel.class, user);
        };
    }

    @Override
    protected boolean hasPermission(User user, EventInstanceVO vo) {
        if(user.hasAdminRole()) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    protected Object createModel(EventInstanceVO vo, User user) {
        return map.apply(vo, user);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends EventInstanceVO, EventInstanceTableDefinition> event) {
        this.notify(event);
    }

}
