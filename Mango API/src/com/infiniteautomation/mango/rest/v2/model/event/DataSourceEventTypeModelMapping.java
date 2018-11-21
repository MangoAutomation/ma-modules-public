/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;

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
    public DataSourceEventTypeModel map(Object from) {
        return new DataSourceEventTypeModel((DataSourceEventType) from);
    }
    
    @Override
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventTypeModel.class));
    }
}
