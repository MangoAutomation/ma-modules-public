/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.type.MissingEventType;

/**
 * @author Terry Packer
 *
 */

public class MissingEventTypeModel extends AbstractEventTypeModel<MissingEventType> {
    
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
