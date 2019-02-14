/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.type.PublisherEventType;
import com.serotonin.m2m2.web.mvc.rest.v1.model.publisher.AbstractPublisherModel;

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
