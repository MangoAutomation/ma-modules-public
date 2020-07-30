/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.rest.latest.model.event.AbstractEventTypeModel;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;

/**
 * @author Terry Packer
 *
 */
public class MaintenanceEventTypeModel extends AbstractEventTypeModel<MaintenanceEventType, MaintenanceEventModel, Void> {

    public MaintenanceEventTypeModel() {
        super(new MaintenanceEventType());
    }
    
    public MaintenanceEventTypeModel(MaintenanceEventType type) {
        super(type);
    }
    
    public MaintenanceEventTypeModel(MaintenanceEventType type, MaintenanceEventModel source) {
        super(type, source);
    }

    @Override
    public MaintenanceEventType toVO() {
        return new MaintenanceEventType(referenceId1);
    }

}
