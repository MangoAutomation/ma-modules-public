/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.vo.event.SetPointEventHandlerVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class SetPointEventHandlerModelMapping implements RestModelJacksonMapping<SetPointEventHandlerVO, SetPointEventHandlerModel> {

    @Override
    public SetPointEventHandlerModel map(Object o, PermissionHolder user, RestModelMapper mapper) {
        return new SetPointEventHandlerModel((SetPointEventHandlerVO)o);
    }

    @Override
    public Class<SetPointEventHandlerModel> toClass() {
        return SetPointEventHandlerModel.class;
    }

    @Override
    public Class<SetPointEventHandlerVO> fromClass() {
        return SetPointEventHandlerVO.class;
    }

    @Override
    public String getTypeName() {
        return "SET_POINT";
    }
}
