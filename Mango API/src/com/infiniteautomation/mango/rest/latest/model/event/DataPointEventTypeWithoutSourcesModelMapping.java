/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class DataPointEventTypeWithoutSourcesModelMapping implements RestModelJacksonMapping<DataPointEventType, DataPointEventTypeWithoutSourcesModel> {

    @Override
    public Class<DataPointEventType> fromClass() {
        return DataPointEventType.class;
    }

    @Override
    public Class<DataPointEventTypeWithoutSourcesModel> toClass() {
        return DataPointEventTypeWithoutSourcesModel.class;
    }

    @Override
    public DataPointEventTypeWithoutSourcesModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        DataPointEventType type = (DataPointEventType) from;
        return new DataPointEventTypeWithoutSourcesModel(type);
    }

    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.DATA_POINT;
    }
}
