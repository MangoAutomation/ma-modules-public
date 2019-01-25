/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.serotonin.m2m2.rt.event.type.PublisherEventType;
import com.serotonin.m2m2.web.mvc.rest.v1.model.publisher.AbstractPublisherModel;

/**
 * @author Terry Packer
 *
 */

public class PublisherEventTypeModel extends AbstractEventTypeModel<PublisherEventType> {
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AbstractPublisherModel<?, ?> publisher;
    
    public PublisherEventTypeModel() {
        super(new PublisherEventType());
    }
    
    public PublisherEventTypeModel(PublisherEventType type) {
        super(type);
    }

    @Override
    public PublisherEventType toVO() {
        return new PublisherEventType(referenceId1, referenceId2);
    }

    /**
     * @param asModel
     */
    public void setPublisher(AbstractPublisherModel<?, ?> publisher) {
        this.publisher = publisher;
    }
    /**
     * @return the publisher
     */
    public AbstractPublisherModel<?, ?> getPublisher() {
        return publisher;
    }
}
