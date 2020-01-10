/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.spring.db.UserCommentTableDefinition;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.web.mvc.rest.v1.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.rest.v1.model.comment.UserCommentModel;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/user-comments")
public class UserCommentWebSocketHandler extends DaoNotificationWebSocketHandler<UserCommentVO, UserCommentTableDefinition>{

    @Override
    protected boolean hasPermission(User user, UserCommentVO vo) {
        if(user.hasAdminRole())
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
    protected void handleDaoEvent(DaoEvent<? extends UserCommentVO, UserCommentTableDefinition> event) {
        this.notify(event);
    }

}
