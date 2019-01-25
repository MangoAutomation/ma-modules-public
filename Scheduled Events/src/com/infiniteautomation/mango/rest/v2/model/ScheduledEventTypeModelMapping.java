/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import org.springframework.stereotype.Component;

import com.serotonin.m2m2.scheduledEvents.ScheduledEventDao;
import com.serotonin.m2m2.scheduledEvents.ScheduledEventType;
import com.serotonin.m2m2.scheduledEvents.ScheduledEventVO;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class ScheduledEventTypeModelMapping implements RestModelMapping<ScheduledEventType, ScheduledEventTypeModel>{

    @Override
    public ScheduledEventTypeModel map(Object o, User user) {
        ScheduledEventType type = (ScheduledEventType)o;
        ScheduledEventVO vo = ScheduledEventDao.getInstance().getScheduledEvent(type.getReferenceId1());
        ScheduledEventTypeModel model;
        if(vo != null)
            model = new ScheduledEventTypeModel(type, new ScheduledEventModel(vo));
        else
            model = new ScheduledEventTypeModel(type);
        return model;
    }

    @Override
    public Class<ScheduledEventTypeModel> toClass() {
        return ScheduledEventTypeModel.class;
    }

    @Override
    public Class<ScheduledEventType> fromClass() {
        return ScheduledEventType.class;
    }

}
