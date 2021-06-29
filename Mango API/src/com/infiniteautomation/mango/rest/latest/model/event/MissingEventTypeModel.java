/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.rt.event.type.MissingEventType;

/**
 * @author Terry Packer
 *
 */

public class MissingEventTypeModel extends AbstractEventTypeModel<MissingEventType, Void, Void> {
    
    public MissingEventTypeModel() {
        super(new MissingEventType());
    }
    
    public MissingEventTypeModel(MissingEventType type) {
        super(type);
    }

    @Override
    public MissingEventType toVO() {
        return new MissingEventType(eventType, subType, referenceId1, referenceId2);
    }
}
