/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.AnalogRangeDetectorRT;
import com.serotonin.m2m2.vo.event.detector.AnalogRangeDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class AnalogRangeEventDetectorRTModel extends TimeDelayedEventDetectorRTModel<AnalogRangeDetectorVO>{

    @ApiModelProperty("State field. Whether the range is currently active or not. This field is used to prevent multiple events being raised during the duration of a single high limit exceed.")
    private boolean rangeActive;
    private long rangeActiveTime;
    private long rangeInactiveTime;

    public AnalogRangeEventDetectorRTModel(AnalogRangeDetectorRT rt) {
        super(rt);
        this.rangeActive = rt.isRangeActive();
        this.rangeActiveTime = rt.getRangeActiveTime();
        this.rangeInactiveTime = rt.getRangeInactiveTime();
    }

    public boolean isRangeActive() {
        return rangeActive;
    }

    public void setRangeActive(boolean rangeActive) {
        this.rangeActive = rangeActive;
    }

    public long getRangeActiveTime() {
        return rangeActiveTime;
    }

    public void setRangeActiveTime(long rangeActiveTime) {
        this.rangeActiveTime = rangeActiveTime;
    }

    public long getRangeInactiveTime() {
        return rangeInactiveTime;
    }

    public void setRangeInactiveTime(long rangeInactiveTime) {
        this.rangeInactiveTime = rangeInactiveTime;
    }

}
