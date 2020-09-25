/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class MaintenanceEventTypeWithoutSourcesModelMapping implements RestModelMapping<MaintenanceEventType, MaintenanceEventTypeWithoutSourcesModel>{

    @Override
    public MaintenanceEventTypeWithoutSourcesModel map(Object o, PermissionHolder user, RestModelMapper mapper) {
        MaintenanceEventType type = (MaintenanceEventType)o;
        return new MaintenanceEventTypeWithoutSourcesModel(type);
    }

    @Override
    public Class<MaintenanceEventTypeWithoutSourcesModel> toClass() {
        return MaintenanceEventTypeWithoutSourcesModel.class;
    }

    @Override
    public Class<MaintenanceEventType> fromClass() {
        return MaintenanceEventType.class;
    }
}
