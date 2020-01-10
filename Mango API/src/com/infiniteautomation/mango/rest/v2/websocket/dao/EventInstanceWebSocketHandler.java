/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.websocket.dao;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.spring.db.EventInstanceTableDefinition;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.EventInstanceVO;
import com.serotonin.m2m2.web.mvc.rest.v1.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.EventInstanceModel;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/event-instances")
public class EventInstanceWebSocketHandler extends DaoNotificationWebSocketHandler<EventInstanceVO, EventInstanceTableDefinition>{

    @Override
    protected boolean hasPermission(User user, EventInstanceVO vo) {
        if(user.hasAdminRole()) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    protected Object createModel(EventInstanceVO vo) {
        return new EventInstanceModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends EventInstanceVO, EventInstanceTableDefinition> event) {
        this.notify(event);
    }

}
