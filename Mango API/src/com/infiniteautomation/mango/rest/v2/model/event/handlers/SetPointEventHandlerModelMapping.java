/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.SetPointEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class SetPointEventHandlerModelMapping implements RestModelMapping<SetPointEventHandlerVO, SetPointEventHandlerModel> {

    @Override
    public SetPointEventHandlerModel map(Object o, User user, RestModelMapper mapper) {
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

}
