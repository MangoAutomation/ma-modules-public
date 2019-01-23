/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.MissingEventType;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class MissingEventTypeModelMapping implements RestModelMapping<MissingEventType, MissingEventTypeModel> {

    @Override
    public Class<MissingEventType> fromClass() {
        return MissingEventType.class;
    }

    @Override
    public Class<MissingEventTypeModel> toClass() {
        return MissingEventTypeModel.class;
    }

    @Override
    public MissingEventTypeModel map(Object from, User user, RestModelMapper mapper) {
        return new MissingEventTypeModel((MissingEventType) from);
    }

}
