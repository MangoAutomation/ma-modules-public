/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;

/**
 * @author Terry Packer
 *
 */
@Component
public class MaintenanceEventTypeModelMapping implements RestModelMapping<MaintenanceEventType, MaintenanceEventTypeModel>{

    @Override
    public MaintenanceEventTypeModel map(Object o) {
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

    @Override
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventTypeModel.class));
    }
    
}
