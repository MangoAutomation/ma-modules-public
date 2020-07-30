/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.AnalogHighLimitDetectorRT;
import com.serotonin.m2m2.vo.event.detector.AnalogHighLimitDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class AnalogHighLimitEventDetectorRTModel extends TimeDelayedEventDetectorRTModel<AnalogHighLimitDetectorVO>{

    @ApiModelProperty("State field. Whether the high limit is currently active or not. This field is used to prevent multiple events being raised during the duration of a single high limit exceed.")
    private boolean highLimitActive;
    private long highLimitActiveTime;
    private long highLimitInactiveTime;

    public AnalogHighLimitEventDetectorRTModel(AnalogHighLimitDetectorRT rt) {
        super(rt);
        this.highLimitActive = rt.isHighLimitActive();
        this.highLimitActiveTime = rt.getHighLimitActiveTime();
        this.highLimitInactiveTime = rt.getHighLimitInactiveTime();
    }

    public boolean isHighLimitActive() {
        return highLimitActive;
    }

    public void setHighLimitActive(boolean highLimitActive) {
        this.highLimitActive = highLimitActive;
    }

    public long getHighLimitActiveTime() {
        return highLimitActiveTime;
    }

    public void setHighLimitActiveTime(long highLimitActiveTime) {
        this.highLimitActiveTime = highLimitActiveTime;
    }

    public long getHighLimitInactiveTime() {
        return highLimitInactiveTime;
    }

    public void setHighLimitInactiveTime(long highLimitInactiveTime) {
        this.highLimitInactiveTime = highLimitInactiveTime;
    }

}
