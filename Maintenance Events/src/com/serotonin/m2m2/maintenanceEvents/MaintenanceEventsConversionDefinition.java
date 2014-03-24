/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.module.DwrConversionDefinition;

public class MaintenanceEventsConversionDefinition extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(MaintenanceEventVO.class);
        addConversion(MaintenanceEventType.class, "bean");
    }
}
