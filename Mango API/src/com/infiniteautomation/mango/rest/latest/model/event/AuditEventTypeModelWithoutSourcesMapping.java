/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.latest.model.RestModelJacksonMapping;
import com.infiniteautomation.mango.rest.latest.model.RestModelMapper;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Terry Packer
 *
 */
@Component
public class AuditEventTypeModelWithoutSourcesMapping implements RestModelJacksonMapping<AuditEventType, AuditEventTypeWithoutSourcesModel> {

    @Override
    public Class<AuditEventType> fromClass() {
        return AuditEventType.class;
    }

    @Override
    public Class<AuditEventTypeWithoutSourcesModel> toClass() {
        return AuditEventTypeWithoutSourcesModel.class;
    }

    @Override
    public AuditEventTypeWithoutSourcesModel map(Object from, PermissionHolder user, RestModelMapper mapper) {
        return new AuditEventTypeWithoutSourcesModel((AuditEventType) from);
    }

    @Override
    public String getTypeName() {
        return EventType.EventTypeNames.AUDIT;
    }

}