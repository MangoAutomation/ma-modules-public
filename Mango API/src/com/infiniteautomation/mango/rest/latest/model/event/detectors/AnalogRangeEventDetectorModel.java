/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.serotonin.m2m2.module.definitions.event.detectors.AnalogRangeEventDetectorDefinition;
import com.serotonin.m2m2.vo.event.detector.AnalogRangeDetectorVO;

/**
 *
 * @author Terry Packer
 */
public class AnalogRangeEventDetectorModel extends TimeoutDetectorModel<AnalogRangeDetectorVO>{

    private double low;
    private double high;
    private boolean withinRange;

    public AnalogRangeEventDetectorModel(AnalogRangeDetectorVO data) {
        fromVO(data);
    }

    public AnalogRangeEventDetectorModel() { }

    @Override
    public void fromVO(AnalogRangeDetectorVO vo) {
        super.fromVO(vo);
        this.low = vo.getLow();
        this.high = vo.getHigh();
        this.withinRange = vo.isWithinRange();
    }

    @Override
    public AnalogRangeDetectorVO toVO() {
        AnalogRangeDetectorVO vo = super.toVO();
        vo.setLow(low);
        vo.setHigh(high);
        vo.setWithinRange(withinRange);
        return vo;
    }

    @Override
    public String getDetectorType() {
        return AnalogRangeEventDetectorDefinition.TYPE_NAME;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public boolean isWithinRange() {
        return withinRange;
    }

    public void setWithinRange(boolean withinRange) {
        this.withinRange = withinRange;
    }

}