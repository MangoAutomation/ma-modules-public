/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.reports;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.reports.handler.ReportEventHandlerVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class ReportEventHandlerModelMapping implements RestModelMapping<ReportEventHandlerVO, ReportEventHandlerModel> {

    @Override
    public ReportEventHandlerModel map(Object o, User user, RestModelMapper mapper) {
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
}
