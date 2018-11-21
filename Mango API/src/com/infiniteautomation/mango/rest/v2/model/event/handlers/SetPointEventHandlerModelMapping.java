/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event.handlers;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.serotonin.m2m2.vo.event.SetPointEventHandlerVO;

/**
 * @author Terry Packer
 *
 */
@Component
public class SetPointEventHandlerModelMapping implements RestModelMapping<SetPointEventHandlerVO, SetPointEventHandlerModel> {

    @Override
    public SetPointEventHandlerModel map(Object o) {
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
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventHandlerModel.class));
    }
}
