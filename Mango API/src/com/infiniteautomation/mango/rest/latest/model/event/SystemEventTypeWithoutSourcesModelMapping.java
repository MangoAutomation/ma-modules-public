/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class SystemEventTypeWithoutSourcesModelMapping implements RestModelJacksonMapping<SystemEventType, SystemEventTypeWithoutSourcesModel> {

    @Override
    public Class<SystemEventType> fromClass() {
        return SystemEventType.class;
    }

    @Override
    public Class<SystemEventTypeWithoutSourcesModel> toClass() {
        return SystemEventTypeWithoutSourcesModel.class;
    }

    @Override
    public SystemEventTypeWithoutSourcesModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        SystemEventType type = (SystemEventType) from;
        return new SystemEventTypeWithoutSourcesModel(type);
    }

    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.SYSTEM;
    }
}
