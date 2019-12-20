/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.module.AuditEventTypeDefinition;

public class AuditEvent extends AuditEventTypeDefinition {
    public static final String TYPE_NAME = "MAINTENANCE_EVENT";

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return "event.audit.maintenanceEvent";
    }
}
