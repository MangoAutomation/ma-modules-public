/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.module.definitions.event.handlers.ScriptEventHandlerDefinition;
import com.serotonin.m2m2.vo.event.ScriptEventHandlerVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Jared Wiltshire
 */
@Component
public class ScriptEventHandlerModelMapping implements AbstractEventHandlerModelMapping<ScriptEventHandlerVO> {

    @Override
    public ScriptEventHandlerModel mapHandler(ScriptEventHandlerVO vo, PermissionHolder user, RestModelMapper mapper) {
        return new ScriptEventHandlerModel(vo);
    }

    @Override
    public Class<ScriptEventHandlerModel> toClass() {
        return ScriptEventHandlerModel.class;
    }

    @Override
    public Class<ScriptEventHandlerVO> fromClass() {
        return ScriptEventHandlerVO.class;
    }

    @Override
    public String getTypeName() {
        return ScriptEventHandlerDefinition.TYPE_NAME;
    }

}
