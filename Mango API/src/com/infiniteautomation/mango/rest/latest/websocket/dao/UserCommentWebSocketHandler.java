/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.comment.UserCommentModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.comment.UserCommentVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/user-comments")
public class UserCommentWebSocketHandler extends DaoNotificationWebSocketHandler<UserCommentVO>{

    @Override
    protected boolean hasPermission(PermissionHolder user, UserCommentVO vo) {
        if(permissionService.hasAdminRole(user)) {
            return true;
        }else if(user instanceof User && ((User)user).getId() == vo.getUserId()){
            return true;
        }else {
            return false;
        }
    }

    @Override
    protected Object createModel(UserCommentVO vo, PermissionHolder user) {
        return new UserCommentModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends UserCommentVO> event) {
        this.notify(event);
    }

}
