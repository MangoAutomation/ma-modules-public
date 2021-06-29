/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.infiniteautomation.mango.rest.latest.model.dataPoint.DataPointModel;
import com.serotonin.m2m2.vo.event.detector.AbstractPointEventDetectorVO;

/**
 *
 * @author Terry Packer
 */
public abstract class AbstractPointEventDetectorModel<T extends AbstractPointEventDetectorVO> extends AbstractEventDetectorModel<T> {

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
}
