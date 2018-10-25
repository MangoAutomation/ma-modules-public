/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.reports.handler;

import com.serotonin.m2m2.module.EventHandlerDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.events.handlers.AbstractEventHandlerModel;

/**
 * Definition to allow reports to run off of an Event
 *
 * @author Terry Packer
 */
public class ReportEventHandlerDefinition extends EventHandlerDefinition<ReportEventHandlerVO>{

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.EventHandlerDefinition#getEventHandlerTypeName()
     */
    @Override
    public String getEventHandlerTypeName() {
        return "REPORT";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.EventHandlerDefinition#getDescriptionKey()
     */
    @Override
    public String getDescriptionKey() {
        return "reports.handler";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.EventHandlerDefinition#createEventHandlerVO()
     */
    @Override
    protected ReportEventHandlerVO createEventHandlerVO() {
        return new ReportEventHandlerVO();
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.EventHandlerDefinition#getModelClass()
     */
    @Override
    public Class<? extends AbstractEventHandlerModel<?>> getModelClass() {
        return ReportEventHandlerModel.class;
    }

}
