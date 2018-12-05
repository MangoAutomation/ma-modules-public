/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.audit.AuditEventInstanceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.audit.AuditEventInstanceModel;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/audit-events")
public class AuditEventWebSocketHandler extends DaoNotificationWebSocketHandler<AuditEventInstanceVO>{

    @Override
    protected boolean hasPermission(User user, AuditEventInstanceVO vo) {
        if(user.hasAdminPermission())
            return true;
        else
            return user.getId() == vo.getUserId();
    }

    @Override
    protected Object createModel(AuditEventInstanceVO vo) {
        return new AuditEventInstanceModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends AuditEventInstanceVO> event) {
        this.notify(event);
    }

}
