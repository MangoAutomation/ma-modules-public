/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.EventInstanceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventInstanceModel;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/event-instances")
public class EventInstanceWebSocketHandler extends DaoNotificationWebSocketHandler<EventInstanceVO>{

    @Override
    protected boolean hasPermission(User user, EventInstanceVO vo) {
        if(user.hasAdminPermission())
            return true;
        else
            return false;
    }

    @Override
    protected Object createModel(EventInstanceVO vo) {
        return new EventInstanceModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends EventInstanceVO> event) {
        this.notify(event);
    }

}
