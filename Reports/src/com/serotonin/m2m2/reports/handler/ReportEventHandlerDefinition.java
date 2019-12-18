/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.reports.handler;

import com.serotonin.m2m2.module.EventHandlerDefinition;

/**
 * Definition to allow reports to run off of an Event
 *
 * @author Terry Packer
 */
public class ReportEventHandlerDefinition extends EventHandlerDefinition<ReportEventHandlerVO>{

    public static final String TYPE_NAME = "REPORT";
    
    @Override
    public String getEventHandlerTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return "reports.handler";
    }

    @Override
    protected ReportEventHandlerVO createEventHandlerVO() {
        return new ReportEventHandlerVO();
    }

}
