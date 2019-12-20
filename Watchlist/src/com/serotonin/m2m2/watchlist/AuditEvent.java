/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import com.serotonin.m2m2.module.AuditEventTypeDefinition;

/**
 * @author Terry Packer
 *
 */
public class AuditEvent extends AuditEventTypeDefinition {
    public static final String TYPE_NAME = "WATCH_LIST";

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return "event.audit.watchlist";
    }

}
