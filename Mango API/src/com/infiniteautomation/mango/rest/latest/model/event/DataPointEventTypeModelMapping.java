/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.serotonin.m2m2.db.dao.DataPointDao;
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

    private final DataPointDao dataPointDao;

    @Autowired
    DataPointEventTypeModelMapping(DataPointDao dataPointDao){
        this.dataPointDao = dataPointDao;
    }

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
        //TODO Mango 4.0 need a better way to get this (and detector) into the model, this was a stopgap solution
        // for a project
        DataPointVO dp = type.getDataPoint();
        if(dp == null) {
            dp = dataPointDao.get(type.getDataPointId());
        }

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
