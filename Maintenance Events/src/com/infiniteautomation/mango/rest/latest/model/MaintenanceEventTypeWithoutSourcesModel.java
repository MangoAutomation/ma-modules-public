/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model;

import com.infiniteautomation.mango.rest.latest.model.event.AbstractEventTypeWithoutSourcesModel;
import com.serotonin.m2m2.maintenanceEvents.MaintenanceEventType;

/**
 * @author Terry Packer
 *
 */
public class MaintenanceEventTypeWithoutSourcesModel extends AbstractEventTypeWithoutSourcesModel<MaintenanceEventType> {

    public MaintenanceEventTypeWithoutSourcesModel() {
        super(new MaintenanceEventType());
    }

    public MaintenanceEventTypeWithoutSourcesModel(MaintenanceEventType type) {
        super(type);
    }

    @Override
    public MaintenanceEventType toVO() {
        return new MaintenanceEventType(referenceId1);
    }

}
