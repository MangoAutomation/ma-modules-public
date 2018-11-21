/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.rest.RestModelMapping;
import com.serotonin.m2m2.rt.event.type.AuditEventType;

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
    public AuditEventTypeModel map(Object from) {
        return new AuditEventTypeModel((AuditEventType) from);
    }
    
    @Override
    public boolean supportsFrom(Object from, Class<?> toClass) {
        return (from.getClass() == fromClass() && (toClass == toClass() || toClass == AbstractEventTypeModel.class));
    }
}
