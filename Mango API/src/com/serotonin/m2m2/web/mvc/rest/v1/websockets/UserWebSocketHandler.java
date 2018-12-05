/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.rest.v1.model.user.UserModel;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 */
@Component
@WebSocketMapping("/websocket/users")
public class UserWebSocketHandler extends DaoNotificationWebSocketHandler<User>{

    @Override
    protected boolean hasPermission(User user, User vo) {
        if(user.hasAdminPermission())
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
    protected void handleDaoEvent(DaoEvent<? extends User> event) {
        this.notify(event);
    }

}
