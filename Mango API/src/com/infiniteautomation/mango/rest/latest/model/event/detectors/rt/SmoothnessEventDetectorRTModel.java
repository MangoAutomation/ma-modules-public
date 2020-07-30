/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.SmoothnessDetectorRT;
import com.serotonin.m2m2.vo.event.detector.SmoothnessDetectorVO;
import com.serotonin.util.queue.ObjectQueue;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class SmoothnessEventDetectorRTModel extends TimeDelayedEventDetectorRTModel<SmoothnessDetectorVO>{

    private ObjectQueue<Double> boxcar = new ObjectQueue<>();

    @ApiModelProperty("State field. Whether the smoothness is currently below the limit or not. This field is used to prevent multiple events being raised during the duration of a single limit breech.")
    private boolean limitBreech;
    private long limitBreechActiveTime;
    private long limitBreechInactiveTime;

    public SmoothnessEventDetectorRTModel(SmoothnessDetectorRT rt) {
        super(rt);
        this.boxcar = rt.getBoxcar();
        this.limitBreech = rt.isLimitBreech();
        this.limitBreechActiveTime = rt.getLimitBreechActiveTime();
        this.limitBreechInactiveTime = rt.getLimitBreechInactiveTime();
    }

    public ObjectQueue<Double> getBoxcar() {
        return boxcar;
    }

    public void setBoxcar(ObjectQueue<Double> boxcar) {
        this.boxcar = boxcar;
    }

    public boolean isLimitBreech() {
        return limitBreech;
    }

    public void setLimitBreech(boolean limitBreech) {
        this.limitBreech = limitBreech;
    }

    public long getLimitBreechActiveTime() {
        return limitBreechActiveTime;
    }

    public void setLimitBreechActiveTime(long limitBreechActiveTime) {
        this.limitBreechActiveTime = limitBreechActiveTime;
    }

    public long getLimitBreechInactiveTime() {
        return limitBreechInactiveTime;
    }

    public void setLimitBreechInactiveTime(long limitBreechInactiveTime) {
        this.limitBreechInactiveTime = limitBreechInactiveTime;
    }

}
