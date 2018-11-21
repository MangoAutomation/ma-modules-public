/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.serotonin.m2m2.rt.event.type.DataPointEventType;

/**
 * @author Terry Packer
 *
 */

public class DataPointEventTypeModel extends AbstractEventTypeModel {

    private Integer dataSourceId;
    
    
    public DataPointEventTypeModel() {
        super(new DataPointEventType());
    }
    
    public DataPointEventTypeModel(DataPointEventType type) {
        super(type);
        this.dataSourceId = type.getDataSourceId();
    }

    /**
     * @return the dataSourceId
     */
    public Integer getDataSourceId() {
        return dataSourceId;
    }

    /**
     * @param dataSourceId the dataSourceId to set
     */
    public void setDataSourceId(Integer dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    
}
