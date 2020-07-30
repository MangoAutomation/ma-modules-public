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
public class DataPointEventTypeModelMapping implements RestModelJacksonMapping<DataPointEventType, DataPointEventTypeModel> {

    @Override
    public Class<DataPointEventType> fromClass() {
        return DataPointEventType.class;
    }

    @Override
    public Class<DataPointEventTypeModel> toClass() {
        return DataPointEventTypeModel.class;
    }

    @Override
    public DataPointEventTypeModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new DataPointEventTypeModel((DataPointEventType) from);
    }
    
    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.DATA_POINT;
    }
}
