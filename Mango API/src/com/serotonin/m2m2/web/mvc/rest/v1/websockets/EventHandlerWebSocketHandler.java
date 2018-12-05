/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.AbstractEventHandlerVO;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/event-handlers")
public class EventHandlerWebSocketHandler extends DaoNotificationWebSocketHandler<AbstractEventHandlerVO<?>>{

    @Override
    protected boolean hasPermission(User user, AbstractEventHandlerVO<?> vo) {
        //TODO Check permissions on point or data source
        if(user.hasAdminPermission())
            return true;
        else
            return false;
    }

    @Override
    protected Object createModel(AbstractEventHandlerVO<?> vo) {
        return vo.asModel();
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends AbstractEventHandlerVO<?>> event) {
        this.notify(event);
    }

}
