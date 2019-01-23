/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.ProcessEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class ProcessEventHandlerModelMapping implements RestModelMapping<ProcessEventHandlerVO, ProcessEventHandlerModel> {

    @Override
    public ProcessEventHandlerModel map(Object o, User user, RestModelMapper mapper) {
        return new ProcessEventHandlerModel((ProcessEventHandlerVO)o);
    }

    @Override
    public Class<ProcessEventHandlerModel> toClass() {
        return ProcessEventHandlerModel.class;
    }

    @Override
    public Class<ProcessEventHandlerVO> fromClass() {
        return ProcessEventHandlerVO.class;
    }
}
