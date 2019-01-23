/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.v2.model.RestModelMapper;
import com.infiniteautomation.mango.rest.v2.model.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.vo.User;

/**
 * @author Terry Packer
 *
 */
@Component
public class AuditEventTypeModelMapping implements RestModelMapping<AuditEventType, AuditEventTypeModel> {

    @Override
    public Class<AuditEventType> fromClass() {
        return AuditEventType.class;
    }

    @Override
    public Class<AuditEventTypeModel> toClass() {
        return AuditEventTypeModel.class;
    }

    @Override
    public AuditEventTypeModel map(Object from, User user, RestModelMapper mapper) {
        return new AuditEventTypeModel((AuditEventType) from);
    }

}
