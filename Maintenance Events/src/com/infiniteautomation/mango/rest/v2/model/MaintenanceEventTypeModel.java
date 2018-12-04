/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import com.infiniteautomation.mango.rest.v2.model.event.AbstractEventTypeModel;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;

/**
 * @author Terry Packer
 *
 */
public class MaintenanceEventTypeModel extends AbstractEventTypeModel<MaintenanceEventType> {

    public MaintenanceEventTypeModel() {
        super(new MaintenanceEventType());
    }
    
    public MaintenanceEventTypeModel(MaintenanceEventType type) {
        super(type);
    }

    @Override
    public MaintenanceEventType toVO() {
        return new MaintenanceEventType(referenceId1);
    }

}
