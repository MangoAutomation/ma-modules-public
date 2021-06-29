/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class DataSourceEventTypeModelMapping implements RestModelJacksonMapping<DataSourceEventType, DataSourceEventTypeModel> {

    @Override
    public Class<DataSourceEventType> fromClass() {
        return DataSourceEventType.class;
    }

    @Override
    public Class<DataSourceEventTypeModel> toClass() {
        return DataSourceEventTypeModel.class;
    }

    @Override
    public DataSourceEventTypeModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new DataSourceEventTypeModel((DataSourceEventType) from);
    }
    
    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.DATA_SOURCE;
    }
}
