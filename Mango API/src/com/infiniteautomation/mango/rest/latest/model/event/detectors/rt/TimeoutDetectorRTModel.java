/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import com.serotonin.m2m2.rt.event.detectors.TimeoutDetectorRT;
import com.serotonin.m2m2.vo.event.detector.TimeoutDetectorVO;

/**
 *
 * @author Terry Packer
 */
public abstract class TimeoutDetectorRTModel<T extends TimeoutDetectorVO<T>> extends AbstractPointEventDetectorRTModel<T> {

    public TimeoutDetectorRTModel(TimeoutDetectorRT<T> rt) {
        super(rt);
        this.eventActive = rt.isEventActive();
        this.listenerName = rt.getListenerName();
    }

    private boolean eventActive;
    private String listenerName;

    public boolean isEventActive() {
        return eventActive;
    }
    public void setEventActive(boolean eventActive) {
        this.eventActive = eventActive;
    }
    public String getListenerName() {
        return listenerName;
    }
    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }


}
