/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.comment.UserCommentModel;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/user-comments")
public class UserCommentWebSocketHandler extends DaoNotificationWebSocketHandler<UserCommentVO>{

    @Override
    protected boolean hasPermission(User user, UserCommentVO vo) {
        if(user.hasAdminPermission())
            return true;
        else
            return user.getId() == vo.getUserId();
    }

    @Override
    protected Object createModel(UserCommentVO vo) {
        return new UserCommentModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends UserCommentVO> event) {
        this.notify(event);
    }

}
