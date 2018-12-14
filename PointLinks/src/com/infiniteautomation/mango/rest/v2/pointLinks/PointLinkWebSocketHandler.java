/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.pointLinks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.v2.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.PointLinkService;
import com.serotonin.m2m2.pointLinks.PointLinkVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
@WebSocketMapping("/websocket/point-links")
public class PointLinkWebSocketHandler extends DaoNotificationWebSocketHandler<PointLinkVO>{

    @Autowired
    private PointLinkService service;
    
    @Override
    protected boolean hasPermission(User user, PointLinkVO vo) {
        try{
            service.ensureReadPermission(vo, user);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    @Override
    protected Object createModel(PointLinkVO vo) {
        return new PointLinkModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends PointLinkVO> event) {
        this.notify(event);
    }
    
}
