/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.MaintenanceEventModel;
import com.infiniteautomation.mango.rest.latest.websocket.DaoNotificationWebSocketHandler;
import com.infiniteautomation.mango.rest.latest.websocket.WebSocketMapping;
import com.infiniteautomation.mango.spring.events.DaoEvent;
import com.infiniteautomation.mango.spring.service.maintenanceEvents.MaintenanceEventsService;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

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
    protected boolean hasPermission(PermissionHolder user, MaintenanceEventVO vo) {
        try{
            service.ensureReadPermission(user, vo);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    @Override
    protected Object createModel(MaintenanceEventVO vo, ApplicationEvent event, PermissionHolder user) {
        return new MaintenanceEventModel(vo);
    }

    @Override
    @EventListener
    protected void handleDaoEvent(DaoEvent<? extends MaintenanceEventVO> event) {
        this.notify(event);
    }

}
