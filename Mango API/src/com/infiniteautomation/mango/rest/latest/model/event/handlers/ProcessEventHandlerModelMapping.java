/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.vo.event.ProcessEventHandlerVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class ProcessEventHandlerModelMapping implements AbstractEventHandlerModelMapping<ProcessEventHandlerVO> {

    @Override
    public ProcessEventHandlerModel mapHandler(ProcessEventHandlerVO vo, PermissionHolder user, RestModelMapper mapper) {
        return new ProcessEventHandlerModel(vo);
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
