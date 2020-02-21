/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import java.util.Date;

import com.serotonin.m2m2.rt.event.detectors.DifferenceDetectorRT;
import com.serotonin.m2m2.vo.event.detector.TimeoutDetectorVO;

/**
 *
 * @author Terry Packer
 */
public abstract class DifferenceDetectorRTModel<T extends TimeoutDetectorVO<T>> extends TimeDelayedEventDetectorRTModel<T>  {

    private Date lastChange;

    public DifferenceDetectorRTModel(DifferenceDetectorRT<T> rt) {
        super(rt);
        this.lastChange = new Date(rt.getLastChange());
    }

    public Date getLastChange() {
        return lastChange;
    }

    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }

}
