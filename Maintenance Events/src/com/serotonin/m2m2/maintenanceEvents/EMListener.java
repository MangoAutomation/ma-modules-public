/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.EventManagerListenerDefinition;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.rt.event.type.EventType;

public class EMListener extends EventManagerListenerDefinition {
    
    /**
     * Suppress events for 
     *   1. data source events listed in the event configuration
     *   2. data point events on a listed data source in the event configuration
     *   3. data point events on a listed data point in the event configuration
     */
    @Override
    public TranslatableMessage autoAckEventWithMessage(EventType eventType) {
        // Data source events can be suppressed by maintenance events.
        if (eventType instanceof DataSourceEventType
                && RTMDefinition.instance.isActiveMaintenanceEventForDataSource(eventType.getDataSourceId()))
            return new TranslatableMessage("events.ackedByMaintenance");

        // Data point events can be suppressed by maintenance events on their data sources.
        if (eventType instanceof DataPointEventType
                && RTMDefinition.instance.isActiveMaintenanceEventForDataSource(eventType.getDataSourceId()))
            return new TranslatableMessage("events.ackedByMaintenance");
        
        //Data point events can be suppressed by maintenance events by the point
        if (eventType instanceof DataPointEventType
                && RTMDefinition.instance.isActiveMaintenanceEventForDataPoint(eventType.getDataPointId()))
            return new TranslatableMessage("events.ackedByMaintenance");

        return null;
    }
}
