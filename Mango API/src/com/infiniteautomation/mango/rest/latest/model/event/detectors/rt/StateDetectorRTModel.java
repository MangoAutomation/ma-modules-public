/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.StateDetectorRT;
import com.serotonin.m2m2.vo.event.detector.TimeoutDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public abstract class StateDetectorRTModel<T extends TimeoutDetectorVO<T>> extends TimeDelayedEventDetectorRTModel<T> {

    public StateDetectorRTModel(StateDetectorRT<T> rt) {
        super(rt);
        this.stateActive = rt.isStateActive();
        this.stateActiveTime = rt.getStateActiveTime();
        this.stateInactiveTime = rt.getStateInactiveTime();
        this.eventActive = rt.isEventActive();

    }

    @ApiModelProperty("State field. Whether the state has been detected or not. This field is used to prevent multiple events being raised during the duration of a single state detection.")
    private boolean stateActive;
    private long stateActiveTime;
    private long stateInactiveTime;
    @ApiModelProperty("State field. Whether the event is currently active or not. This field is used to prevent multiple events being raised during the duration of a single state detection.")
    private boolean eventActive;

    public boolean isStateActive() {
        return stateActive;
    }
    public void setStateActive(boolean stateActive) {
        this.stateActive = stateActive;
    }
    public long getStateActiveTime() {
        return stateActiveTime;
    }
    public void setStateActiveTime(long stateActiveTime) {
        this.stateActiveTime = stateActiveTime;
    }
    public long getStateInactiveTime() {
        return stateInactiveTime;
    }
    public void setStateInactiveTime(long stateInactiveTime) {
        this.stateInactiveTime = stateInactiveTime;
    }
    @Override
    public boolean isEventActive() {
        return eventActive;
    }
    @Override
    public void setEventActive(boolean eventActive) {
        this.eventActive = eventActive;
    }



}
