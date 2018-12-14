/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.maintenanceEvents.MaintenanceEventsService;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.spring.WebSocketMapping;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 *
 * @author Terry Packer
 */
@Component
@WebSocketMapping("/websocket/maintenance-events")
public class MaintenanceEventWebSocketHandler extends DaoNotificationWebSocketHandler<MaintenanceEventVO> {

    @Autowired
    private  MaintenanceEventsService service;

    @Override
    protected boolean hasPermission(User user, MaintenanceEventVO vo) {
        try{
            service.ensureReadPermission(user, vo);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    @Override
    protected Object createModel(MaintenanceEventVO vo) {
        return new MaintenanceEventModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends MaintenanceEventVO> event) {
        this.notify(event);
    }

}
