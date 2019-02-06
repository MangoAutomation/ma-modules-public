/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors;

import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;

/**
 *
 * @author Terry Packer
 */
public abstract class AbstractPointEventDetectorModel<T extends AbstractPointEventDetectorVO<T>> extends AbstractEventDetectorModel<T> {

    public AbstractPointEventDetectorModel(T data) {
        fromVO(data);
    }
    
    public AbstractPointEventDetectorModel() { }

    protected DataPointModel dataPoint;
    
    @Override
    public void fromVO(T vo) {
        super.fromVO(vo);
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
    
    public int getDataPointId(){
        return sourceId;
    }
    
    public void setDataPointId(int dataPointId) {
        this.sourceId = dataPointId;
    }
}
