/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.comment.UserCommentModel;
import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.comment.UserCommentVO;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/user-comments")
public class UserCommentWebSocketHandler extends DaoNotificationWebSocketHandler<UserCommentVO>{

    @Override
    protected boolean hasPermission(User user, UserCommentVO vo) {
        if(user.hasAdminRole())
            return true;
        else
            return user.getId() == vo.getUserId();
    }

    @Override
    protected Object createModel(UserCommentVO vo, User user) {
        return new UserCommentModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends UserCommentVO> event) {
        this.notify(event);
    }

}
