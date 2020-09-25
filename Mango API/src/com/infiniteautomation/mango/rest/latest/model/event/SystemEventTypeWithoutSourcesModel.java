/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.rt.event.type.SystemEventType;

/**
 * @author Terry Packer
 *
 */

public class SystemEventTypeWithoutSourcesModel extends AbstractEventTypeWithoutSourcesModel<SystemEventType> {

    public SystemEventTypeWithoutSourcesModel() {
        super(new SystemEventType());
    }

    public SystemEventTypeWithoutSourcesModel(SystemEventType type) {
        super(type);
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @Override
    public SystemEventType toVO() {
        return new SystemEventType(subType, referenceId1, duplicateHandling);
    }
}
