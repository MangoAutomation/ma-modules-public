/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.maintenanceEvents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.service.maintenanceEvents.MaintenanceEventsService;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 *
 * @author Terry Packer
 */
@Component("maintenanceEventWebSocketHandler")
public class MaintenanceEventWebSocketHandler extends DaoNotificationWebSocketHandler<MaintenanceEventVO> {

    @Autowired
    private  MaintenanceEventsService service;
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler#getDaoBeanName()
     */
    @Override
    public String getDaoBeanName() {
        return "maintenanceEventDao";
    }
    
    @Override
    protected boolean hasPermission(User user, MaintenanceEventVO vo) {
        try{
            service.ensureReadPermission(vo, user);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    @Override
    protected Object createModel(MaintenanceEventVO vo) {
        return new MaintenanceEventModel(vo);
    }
    
}
