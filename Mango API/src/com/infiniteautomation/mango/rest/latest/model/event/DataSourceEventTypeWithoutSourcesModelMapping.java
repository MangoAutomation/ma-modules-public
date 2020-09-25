/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
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
public class DataSourceEventTypeWithoutSourcesModelMapping implements RestModelJacksonMapping<DataSourceEventType, DataSourceEventTypeWithoutSourcesModel> {

    @Override
    public Class<DataSourceEventType> fromClass() {
        return DataSourceEventType.class;
    }

    @Override
    public Class<DataSourceEventTypeWithoutSourcesModel> toClass() {
        return DataSourceEventTypeWithoutSourcesModel.class;
    }

    @Override
    public DataSourceEventTypeWithoutSourcesModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new DataSourceEventTypeWithoutSourcesModel((DataSourceEventType) from);
    }

    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.DATA_SOURCE;
    }
}
