/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event;

import com.serotonin.m2m2.rt.event.type.DataPointEventType;

/**
 * @author Terry Packer
 *
 */

public class DataPointEventTypeWithoutSourcesModel extends AbstractEventTypeWithoutSourcesModel<DataPointEventType> {

    public DataPointEventTypeWithoutSourcesModel() {
        super(new DataPointEventType());
    }

    public DataPointEventTypeWithoutSourcesModel(DataPointEventType type) {
        super(type);
    }

    @Override
    public DataPointEventType toVO() {
        DataPointEventType type = new DataPointEventType(referenceId1, referenceId2);
        type.setDuplicateHandling(duplicateHandling);
        return type;
    }

}
