/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.serotonin.m2m2.vo.event.ProcessEventHandlerVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class ProcessEventHandlerModelMapping implements RestModelJacksonMapping<ProcessEventHandlerVO, ProcessEventHandlerModel> {

    @Override
    public ProcessEventHandlerModel map(Object o, PermissionHolder user, RestModelMapper mapper) {
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
    
    @Override
    public String getTypeName() {
        return "PROCESS";
    }
}
