/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.rt.event.type.PublisherEventType;

/**
 * @author Terry Packer
 *
 */

public class PublisherEventTypeWithoutSourcesModel extends AbstractEventTypeWithoutSourcesModel<PublisherEventType> {

    public PublisherEventTypeWithoutSourcesModel() {
        super(new PublisherEventType());
    }

    public PublisherEventTypeWithoutSourcesModel(PublisherEventType type) {
        super(type);
    }

    @Override
    public PublisherEventType toVO() {
        return new PublisherEventType(referenceId1, referenceId2);
    }
}
