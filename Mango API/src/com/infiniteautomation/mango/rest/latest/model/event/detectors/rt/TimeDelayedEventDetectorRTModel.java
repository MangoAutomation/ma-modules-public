/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import java.util.Date;

import com.serotonin.m2m2.rt.event.detectors.TimeDelayedEventDetectorRT;
import com.serotonin.m2m2.vo.event.detector.TimeoutDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public abstract class TimeDelayedEventDetectorRTModel<T extends TimeoutDetectorVO<T>> extends TimeoutDetectorRTModel<T> {

    @ApiModelProperty("The timestamp for when the condition has gone active, not to be confused with when the event goes active")
    private Date conditionActive;

    public TimeDelayedEventDetectorRTModel(TimeDelayedEventDetectorRT<T> rt) {
        super(rt);
        if(rt.getConditionActiveTime() > 0) {
            this.conditionActive = new Date(rt.getConditionActiveTime());
        }
    }

    public Date getConditionActive() {
        return conditionActive;
    }

    public void setConditionActive(Date conditionActive) {
        this.conditionActive = conditionActive;
    }

}
