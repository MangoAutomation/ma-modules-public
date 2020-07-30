/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.AnalogLowLimitDetectorRT;
import com.serotonin.m2m2.vo.event.detector.AnalogLowLimitDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class AnalogLowLimitEventDetectorRTModel extends TimeDelayedEventDetectorRTModel<AnalogLowLimitDetectorVO>{

    @ApiModelProperty("State field. Whether the low limit is currently active or not. This field is used to prevent multiple events being raised during the duration of a single low limit event.")
    private boolean lowLimitActive;
    private long lowLimitActiveTime;
    private long lowLimitInactiveTime;

    public AnalogLowLimitEventDetectorRTModel(AnalogLowLimitDetectorRT rt) {
        super(rt);
        this.lowLimitActive = rt.isLowLimitActive();
        this.lowLimitActiveTime = rt.getLowLimitActiveTime();
        this.lowLimitInactiveTime = rt.getLowLimitInactiveTime();
    }

    public boolean isLowLimitActive() {
        return lowLimitActive;
    }

    public void setLowLimitActive(boolean lowLimitActive) {
        this.lowLimitActive = lowLimitActive;
    }

    public long getLowLimitActiveTime() {
        return lowLimitActiveTime;
    }

    public void setLowLimitActiveTime(long lowLimitActiveTime) {
        this.lowLimitActiveTime = lowLimitActiveTime;
    }

    public long getLowLimitInactiveTime() {
        return lowLimitInactiveTime;
    }

    public void setLowLimitInactiveTime(long lowLimitInactiveTime) {
        this.lowLimitInactiveTime = lowLimitInactiveTime;
    }

}
