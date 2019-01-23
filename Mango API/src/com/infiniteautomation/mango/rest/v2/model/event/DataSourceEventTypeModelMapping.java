/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class DataSourceEventTypeModelMapping implements RestModelMapping<DataSourceEventType, DataSourceEventTypeModel> {

    @Override
    public Class<DataSourceEventType> fromClass() {
        return DataSourceEventType.class;
    }

    @Override
    public Class<DataSourceEventTypeModel> toClass() {
        return DataSourceEventTypeModel.class;
    }

    @Override
    public DataSourceEventTypeModel map(Object from, User user, RestModelMapper mapper) {
        return new DataSourceEventTypeModel((DataSourceEventType) from);
    }
}
