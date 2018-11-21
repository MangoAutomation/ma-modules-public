/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.type.PublisherEventType;

/**
 * @author Terry Packer
 *
 */

public class PublisherEventTypeModel extends AbstractEventTypeModel<PublisherEventType> {
    
    public PublisherEventTypeModel() {
        super(new PublisherEventType());
    }
    
    public PublisherEventTypeModel(PublisherEventType type) {
        super(type);
    }
}
