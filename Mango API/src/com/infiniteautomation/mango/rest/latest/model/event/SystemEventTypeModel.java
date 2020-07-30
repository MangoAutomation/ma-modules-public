/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.rt.event.type.SystemEventType;

/**
 * @author Terry Packer
 *
 */

public class SystemEventTypeModel extends AbstractEventTypeModel<SystemEventType, Object, Object> {

    public SystemEventTypeModel() {
        super(new SystemEventType());
    }

    public SystemEventTypeModel(SystemEventType type) {
        super(type);
    }

    public SystemEventTypeModel(SystemEventType type, Object reference1) {
        super(type, reference1);
    }

    public SystemEventTypeModel(SystemEventType type, Object reference1, Object reference2) {
        super(type, reference1, reference2);
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @Override
    public SystemEventType toVO() {
        return new SystemEventType(subType, referenceId1, duplicateHandling);
    }
}
