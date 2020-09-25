/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.rt.event.type.MissingEventType;

/**
 * @author Terry Packer
 *
 */

public class MissingEventTypeWithoutSourcesModel extends AbstractEventTypeWithoutSourcesModel<MissingEventType> {

    public MissingEventTypeWithoutSourcesModel() {
        super(new MissingEventType());
    }

    public MissingEventTypeWithoutSourcesModel(MissingEventType type) {
        super(type);
    }

    @Override
    public MissingEventType toVO() {
        return new MissingEventType(eventType, subType, referenceId1, referenceId2);
    }
}
