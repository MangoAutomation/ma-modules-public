/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.websockets;

import org.springframework.stereotype.Component;

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
@WebSocketMapping("/v1/websocket/templates")
public class TemplateWebSocketHandler extends DaoNotificationWebSocketHandler<BaseTemplateVO<?>>{

    @Override
    protected boolean hasPermission(User user, BaseTemplateVO<?> vo) {
        return Permissions.hasPermission(user, vo.getReadPermission());
    }

    @Override
    protected Object createModel(BaseTemplateVO<?> vo) {
        throw new RuntimeException("Un-implemented!");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends BaseTemplateVO<?>> supportedClass() {
        return (Class<? extends BaseTemplateVO<?>>) BaseTemplateVO.class;
    }

}
