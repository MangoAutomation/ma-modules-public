/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import com.serotonin.m2m2.module.DwrConversionDefinition;

public class ScheduledEventConversionDefinition extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(ScheduledEventVO.class);
        addConversion(ScheduledEventType.class, "bean");
    }
}
