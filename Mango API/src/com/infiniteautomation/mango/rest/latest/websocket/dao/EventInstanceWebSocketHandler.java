/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.event.EventInstanceModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.event.EventInstanceVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/event-instances")
public class EventInstanceWebSocketHandler extends DaoNotificationWebSocketHandler<EventInstanceVO> {

    private final BiFunction<EventInstanceVO, PermissionHolder, EventInstanceModel> map;

    @Autowired
    public EventInstanceWebSocketHandler(RestModelMapper modelMapper) {
        this.map = (vo, user) -> {
            return modelMapper.map(vo, EventInstanceModel.class, user);
        };
    }

    @Override
    protected boolean hasPermission(PermissionHolder user, EventInstanceVO vo) {
        if(permissionService.hasAdminRole(user)) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    protected Object createModel(EventInstanceVO vo, PermissionHolder user) {
        return map.apply(vo, user);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends EventInstanceVO> event) {
        this.notify(event);
    }

}
