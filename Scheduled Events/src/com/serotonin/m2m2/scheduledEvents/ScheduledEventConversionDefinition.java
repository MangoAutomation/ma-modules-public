/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import com.serotonin.m2m2.module.DwrConversionDefinition;

public class ScheduledEventConversionDefinition extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(ScheduledEventVO.class);
    }
}
