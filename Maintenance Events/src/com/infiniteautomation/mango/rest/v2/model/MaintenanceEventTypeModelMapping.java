/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class MaintenanceEventTypeModelMapping implements RestModelMapping<MaintenanceEventType, MaintenanceEventTypeModel>{

    @Override
    public MaintenanceEventTypeModel map(Object o, User user) {
        return new MaintenanceEventTypeModel((MaintenanceEventType)o);
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
