/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.type.DataSourceEventType;

/**
 * @author Terry Packer
 *
 */

public class DataSourceEventTypeModel extends AbstractEventTypeModel {
    
    public DataSourceEventTypeModel() {
        super(new DataSourceEventType());
    }
    
    public DataSourceEventTypeModel(DataSourceEventType type) {
        super(type);
    }
}
