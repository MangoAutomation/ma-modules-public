/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.spring.db.UserTableDefinition;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;

/**
 * @author Terry Packer
 */
@Component
@WebSocketMapping("/websocket/users")
public class UserWebSocketHandler extends DaoNotificationWebSocketHandler<User, UserTableDefinition>{

    @Override
    protected boolean hasPermission(User user, User vo) {
        if(user.hasAdminRole())
            return true;
        else
            return user.getId() == vo.getId();
    }

    @Override
    protected Object createModel(User vo) {
        return new UserModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends User, UserTableDefinition> event) {
        this.notify(event);
    }

}
