/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class DataPointEventTypeModelMapping implements RestModelMapping<DataPointEventType, DataPointEventTypeModel> {

    @Override
    public Class<DataPointEventType> fromClass() {
        return DataPointEventType.class;
    }

    @Override
    public Class<DataPointEventTypeModel> toClass() {
        return DataPointEventTypeModel.class;
    }

    @Override
    public DataPointEventTypeModel map(Object from, User user, RestModelMapper mapper) {
        return new DataPointEventTypeModel((DataPointEventType) from);
    }
}
