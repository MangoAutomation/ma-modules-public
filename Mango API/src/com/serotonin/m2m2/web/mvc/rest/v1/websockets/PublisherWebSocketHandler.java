/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/publishers")
public class PublisherWebSocketHandler extends DaoNotificationWebSocketHandler<PublisherVO<?>> {

    @Override
    protected boolean hasPermission(User user, PublisherVO<?> vo) {
        if(user.hasAdminPermission())
            return true;
        else
            return false; //TODO Implement permissions for publishers... Permissions.hasPublisherPermission(user, vo);
    }

    @Override
    protected Object createModel(PublisherVO<?> vo) {
        return vo.asModel();
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends PublisherVO<?>> event) {
        this.notify(event);
    }
}
