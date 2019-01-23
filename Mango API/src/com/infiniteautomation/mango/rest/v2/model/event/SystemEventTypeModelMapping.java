/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class SystemEventTypeModelMapping implements RestModelMapping<SystemEventType, SystemEventTypeModel> {

    @Override
    public Class<SystemEventType> fromClass() {
        return SystemEventType.class;
    }

    @Override
    public Class<SystemEventTypeModel> toClass() {
        return SystemEventTypeModel.class;
    }

    @Override
    public SystemEventTypeModel map(Object from, User user, RestModelMapper mapper) {
        return new SystemEventTypeModel((SystemEventType) from);
    }
}
