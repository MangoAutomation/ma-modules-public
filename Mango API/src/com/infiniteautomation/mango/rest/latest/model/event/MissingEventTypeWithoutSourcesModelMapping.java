/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.MissingEventType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class MissingEventTypeWithoutSourcesModelMapping implements RestModelJacksonMapping<MissingEventType, MissingEventTypeWithoutSourcesModel> {

    @Override
    public Class<MissingEventType> fromClass() {
        return MissingEventType.class;
    }

    @Override
    public Class<MissingEventTypeWithoutSourcesModel> toClass() {
        return MissingEventTypeWithoutSourcesModel.class;
    }

    @Override
    public MissingEventTypeWithoutSourcesModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new MissingEventTypeWithoutSourcesModel((MissingEventType) from);
    }

    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.MISSING;
    }

}
