/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventDao;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class MaintenanceEventTypeModelMapping implements RestModelMapping<MaintenanceEventType, MaintenanceEventTypeModel>{

    @Override
    public MaintenanceEventTypeModel map(Object o, PermissionHolder user, RestModelMapper mapper) {
        MaintenanceEventType type = (MaintenanceEventType)o;
        MaintenanceEventVO vo = MaintenanceEventDao.getInstance().get(type.getReferenceId1());
        MaintenanceEventTypeModel model;
        if(vo != null)
            model = new MaintenanceEventTypeModel(type, new MaintenanceEventModel(vo));
        else
            model = new MaintenanceEventTypeModel(type);
        return model;
    }

    @Override
    public Class<MaintenanceEventTypeModel> toClass() {
        return MaintenanceEventTypeModel.class;
    }

    @Override
    public Class<MaintenanceEventType> fromClass() {
        return MaintenanceEventType.class;
    }
}
