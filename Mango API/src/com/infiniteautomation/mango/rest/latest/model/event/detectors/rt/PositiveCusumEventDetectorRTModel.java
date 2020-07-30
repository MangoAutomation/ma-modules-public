/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.PositiveCusumDetectorRT;
import com.serotonin.m2m2.vo.event.detector.PositiveCusumDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class PositiveCusumEventDetectorRTModel extends TimeDelayedEventDetectorRTModel<PositiveCusumDetectorVO>{

    @ApiModelProperty("State field. The current negative CUSUM for the point.")
    private double cusum;

    @ApiModelProperty("State field. Whether the negative CUSUM is currently active or not. This field is used to prevent multiple events being raised during the duration of a single negative CUSUM exceed.")
    private boolean positiveCusumActive;
    private long positiveCusumActiveTime;
    private long positiveCusumInactiveTime;

    public PositiveCusumEventDetectorRTModel(PositiveCusumDetectorRT rt) {
        super(rt);
        this.cusum = rt.getCusum();
        this.positiveCusumActive = rt.isPositiveCusumActive();
        this.positiveCusumActiveTime = rt.getPositiveCusumActiveTime();
        this.positiveCusumInactiveTime = rt.getPositiveCusumInactiveTime();
    }

    public double getCusum() {
        return cusum;
    }

    public void setCusum(double cusum) {
        this.cusum = cusum;
    }

    public boolean isPositiveCusumActive() {
        return positiveCusumActive;
    }

    public void setPositiveCusumActive(boolean positiveCusumActive) {
        this.positiveCusumActive = positiveCusumActive;
    }

    public long getPositiveCusumActiveTime() {
        return positiveCusumActiveTime;
    }

    public void setPositiveCusumActiveTime(long positiveCusumActiveTime) {
        this.positiveCusumActiveTime = positiveCusumActiveTime;
    }

    public long getPositiveCusumInactiveTime() {
        return positiveCusumInactiveTime;
    }

    public void setPositiveCusumInactiveTime(long positiveCusumInactiveTime) {
        this.positiveCusumInactiveTime = positiveCusumInactiveTime;
    }

}
