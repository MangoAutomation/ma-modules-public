/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.type.AuditEventType;

/**
 * @author Terry Packer
 *
 */

public class AuditEventTypeModel extends AbstractEventTypeModel {

    public AuditEventTypeModel() {
        super(new AuditEventType());
    }
    
    public AuditEventTypeModel(AuditEventType type) {
        super(type);
    }
    
}
