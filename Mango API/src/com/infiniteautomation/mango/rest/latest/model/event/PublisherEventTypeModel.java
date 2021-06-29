/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.infiniteautomation.mango.rest.latest.model.publisher.AbstractPublisherModel;
import com.serotonin.m2m2.rt.event.type.PublisherEventType;

/**
 * @author Terry Packer
 *
 */

public class PublisherEventTypeModel extends AbstractEventTypeModel<PublisherEventType, AbstractPublisherModel<?,?>, String> {
    
    public PublisherEventTypeModel() {
        super(new PublisherEventType());
    }
    
    public PublisherEventTypeModel(PublisherEventType type) {
        super(type);
    }

    public PublisherEventTypeModel(PublisherEventType type, AbstractPublisherModel<?,?> reference1) {
        super(type, reference1);
    }
    
    public PublisherEventTypeModel(PublisherEventType type, AbstractPublisherModel<?,?> reference1, String reference2) {
        super(type, reference1, reference2);
    }

    
    @Override
    public PublisherEventType toVO() {
        return new PublisherEventType(referenceId1, referenceId2);
    }
}
