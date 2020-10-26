/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.websocket.dao;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.user.UserModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.MangoWebSocketResponseModel;
import com.infiniteautomation.mango.rest.latest.websocket.MangoWebSocketResponseStatus;
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

    /*
     * Overriden to use username instead of xid
     */
    @Override
    protected Object createNotification(String action, User vo, User originalVo, PermissionHolder user) {
        Integer id = vo.getId();
        String xid = vo.getUsername();
        String originalXid = (originalVo != null) ? originalVo.getUsername() : null;

        DaoNotificationModel payload;
        if(StringUtils.equals(action, "delete")) {
            payload = new DaoNotificationModel(action, id, xid, null, originalXid);
        }else {
            Object model = createModel(vo, user);
            if (model == null) {
                return null;
            }
            payload = new DaoNotificationModel("create".equals(action) ? "add" : action, id, xid, model, originalXid);
        }
        return new MangoWebSocketResponseModel(MangoWebSocketResponseStatus.OK, payload);
    }
}
