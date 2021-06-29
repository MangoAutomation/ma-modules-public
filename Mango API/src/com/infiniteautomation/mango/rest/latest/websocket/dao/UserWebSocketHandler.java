/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.user.UserModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 */
@Component
@WebSocketMapping("/websocket/users")
public class UserWebSocketHandler extends DaoNotificationWebSocketHandler<User>{

    @Override
    protected boolean hasPermission(PermissionHolder user, User vo) {
        if (permissionService.hasAdminRole(user)) {
            return true;
        }
        return user.getUser() != null && user.getUser().getId() == vo.getId();
    }

    @Override
    protected Object createModel(User vo, ApplicationEvent event, PermissionHolder user) {
        return new UserModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends User> event) {
        this.notify(event);
    }

}
