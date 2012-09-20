/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.EventManagerListenerDefinition;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.rt.event.type.EventType;

public class EMListener extends EventManagerListenerDefinition {
    @Override
    public TranslatableMessage autoAckEventWithMessage(EventType eventType) {
        // Data source events can be suppressed by maintenance events.
        if (eventType instanceof DataSourceEventType
                && RTMDefinition.instance.isActiveMaintenanceEvent(eventType.getDataSourceId()))
            return new TranslatableMessage("events.ackedByMaintenance");

        // Data point events can be suppressed by maintenance events on their data sources.
        if (eventType instanceof DataPointEventType
                && RTMDefinition.instance.isActiveMaintenanceEvent(eventType.getDataSourceId()))
            return new TranslatableMessage("events.ackedByMaintenance");

        return null;
    }
}
