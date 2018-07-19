/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.infiniteautomation.mango.rest.maintenanceEvents.MaintenanceEventModel;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.websocket.DaoNotificationWebSocketHandler;

/**
 *
 * @author Terry Packer
 */
public class MaintenanceEventWebSocketHandler extends DaoNotificationWebSocketHandler<MaintenanceEventVO> {

    @Override
    protected boolean hasPermission(User user, MaintenanceEventVO vo) {
        //TODO is this based on permissions to the data source?
        return user.isAdmin();
    }

    @Override
    protected Object createModel(MaintenanceEventVO vo) {
        return new MaintenanceEventModel(vo);
    }
}
