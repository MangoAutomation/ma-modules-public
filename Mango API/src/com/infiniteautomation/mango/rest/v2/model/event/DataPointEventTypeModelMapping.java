/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.rt.event.type.EventType;

/**
 * @author Terry Packer
 *
 */
@Component
public class DataPointEventTypeModelMapping implements RestModelMapping<DataPointEventTypeModel, DataPointEventType> {

    @Override
    public Class<DataPointEventType> fromClass() {
        return DataPointEventType.class;
    }
    
    @Override
    public Class<DataPointEventTypeModel> toClass() {
        return DataPointEventTypeModel.class;
    }

    @Override
    public DataPointEventTypeModel map(Object from) {
        return new DataPointEventTypeModel((DataPointEventType) from);
    }

    @Override
    public String getTypeId() {
        return EventType.EventTypeNames.DATA_POINT;
    }
    
    @Override
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventTypeModel.class));
    }
}
