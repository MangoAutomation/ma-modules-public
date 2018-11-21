/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.reports;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.infiniteautomation.mango.rest.v2.model.event.handlers.AbstractEventHandlerModel;
import com.serotonin.m2m2.reports.handler.ReportEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class ReportEventHandlerModelMapping implements RestModelMapping<ReportEventHandlerVO, ReportEventHandlerModel> {

    @Override
    public ReportEventHandlerModel map(Object o) {
        return new ReportEventHandlerModel((ReportEventHandlerVO)o);
    }

    @Override
    public Class<ReportEventHandlerModel> toClass() {
        return ReportEventHandlerModel.class;
    }

    @Override
    public Class<ReportEventHandlerVO> fromClass() {
        return ReportEventHandlerVO.class;
    }
    
    @Override
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventHandlerModel.class));
    }
}
