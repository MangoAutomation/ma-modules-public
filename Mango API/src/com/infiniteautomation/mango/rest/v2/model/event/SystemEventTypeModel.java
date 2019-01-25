/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.type.SystemEventType;

/**
 * @author Terry Packer
 *
 */

public class SystemEventTypeModel extends AbstractEventTypeModel<SystemEventType, Void> {
    
    public SystemEventTypeModel() {
        super(new SystemEventType());
    }
    
    public SystemEventTypeModel(SystemEventType type) {
        super(type);
    }

    @Override
    public SystemEventType toVO() {
        return new SystemEventType(subType, referenceId1, duplicateHandling);
    }
}
