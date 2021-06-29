/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.DataPointVO;
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
        DataPointEventType type = (DataPointEventType) from;
        DataPointVO dp = (DataPointVO)type.getReference1();

        DataPointModel dpModel;
        if(dp != null) {
            dpModel = mapper.map(dp, DataPointModel.class, user);
        }else {
            dpModel = null;
        }
        return new DataPointEventTypeModel(type, dpModel);
    }

    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.DATA_POINT;
    }

}
