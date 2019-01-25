/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;
import com.serotonin.m2m2.rt.event.type.DataPointEventType;

/**
 * @author Terry Packer
 *
 */

public class DataPointEventTypeModel extends AbstractEventTypeModel<DataPointEventType> {

    private Integer dataSourceId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DataPointModel dataPoint;
    
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

    /**
     * @return the dataPoint
     */
    public DataPointModel getDataPoint() {
        return dataPoint;
    }

    /**
     * @param dataPoint the dataPoint to set
     */
    public void setDataPoint(DataPointModel dataPoint) {
        this.dataPoint = dataPoint;
    }

    @Override
    public DataPointEventType toVO() {
        return new DataPointEventType(dataSourceId, referenceId1, referenceId2, duplicateHandling);
    }

    
}
