/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.user.UserModel;
import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
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
        if(permissionService.hasAdminRole(user)) {
            return true;
        }else if(user instanceof User && ((User)user).getId() == vo.getId()){
            return true;
        }else {
            return false;
        }
    }

    @Override
    protected Object createModel(User vo, PermissionHolder user) {
        return new UserModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends User> event) {
        this.notify(event);
    }

}
