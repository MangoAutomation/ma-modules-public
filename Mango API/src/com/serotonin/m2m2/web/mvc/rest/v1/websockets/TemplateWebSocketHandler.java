/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.template.BaseTemplateVO;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/templates")
public class TemplateWebSocketHandler extends DaoNotificationWebSocketHandler<BaseTemplateVO<?>>{

    @Override
    protected boolean hasPermission(User user, BaseTemplateVO<?> vo) {
        return Permissions.hasPermission(user, vo.getReadPermission());
    }

    @Override
    protected Object createModel(BaseTemplateVO<?> vo) {
        throw new RuntimeException("Un-implemented!");
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends BaseTemplateVO<?>> event) {
        this.notify(event);
    }

}
