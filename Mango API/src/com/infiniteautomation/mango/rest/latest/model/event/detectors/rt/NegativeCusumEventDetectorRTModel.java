/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.NegativeCusumDetectorRT;
import com.serotonin.m2m2.vo.event.detector.NegativeCusumDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class NegativeCusumEventDetectorRTModel extends TimeDelayedEventDetectorRTModel<NegativeCusumDetectorVO>{

    @ApiModelProperty("State field. The current negative CUSUM for the point.")
    private double cusum;

    @ApiModelProperty("State field. Whether the negative CUSUM is currently active or not. This field is used to prevent multiple events being raised during the duration of a single negative CUSUM exceed.")
    private boolean negativeCusumActive;
    private long negativeCusumActiveTime;
    private long negativeCusumInactiveTime;

    public NegativeCusumEventDetectorRTModel(NegativeCusumDetectorRT rt) {
        super(rt);
        this.cusum = rt.getCusum();
        this.negativeCusumActive = rt.isNegativeCusumActive();
        this.negativeCusumActiveTime = rt.getNegativeCusumActiveTime();
        this.negativeCusumInactiveTime = rt.getNegativeCusumInactiveTime();
    }

    public double getCusum() {
        return cusum;
    }

    public void setCusum(double cusum) {
        this.cusum = cusum;
    }

    public boolean isNegativeCusumActive() {
        return negativeCusumActive;
    }

    public void setNegativeCusumActive(boolean negativeCusumActive) {
        this.negativeCusumActive = negativeCusumActive;
    }

    public long getNegativeCusumActiveTime() {
        return negativeCusumActiveTime;
    }

    public void setNegativeCusumActiveTime(long negativeCusumActiveTime) {
        this.negativeCusumActiveTime = negativeCusumActiveTime;
    }

    public long getNegativeCusumInactiveTime() {
        return negativeCusumInactiveTime;
    }

    public void setNegativeCusumInactiveTime(long negativeCusumInactiveTime) {
        this.negativeCusumInactiveTime = negativeCusumInactiveTime;
    }

}
