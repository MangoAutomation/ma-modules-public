/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import java.util.ArrayList;
import java.util.List;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.event.detectors.StateChangeCountDetectorRT;
import com.serotonin.m2m2.vo.event.detector.StateChangeCountDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class StateChangeCountEventDetectorRTModel extends TimeoutDetectorRTModel<StateChangeCountDetectorVO>{

    @ApiModelProperty("State field. The point values that have accumulated so far. Each call to pointChanged will drop off the values beyond the duration.")
    private List<PointValueTimeModel> pointValues;
    @ApiModelProperty("State field. The time at which an event will be raised if conditions are right.")
    private long eventActiveTime;

    public StateChangeCountEventDetectorRTModel(StateChangeCountDetectorRT rt) {
        super(rt);
        List<PointValueTime> period = rt.getPointValues();
        this.pointValues = new ArrayList<>();
        for(PointValueTime pvt : period) {
            this.pointValues.add(new PointValueTimeModel(pvt));
        }
        this.eventActiveTime = rt.getEventActiveTime();
    }

    public List<PointValueTimeModel> getPointValues() {
        return pointValues;
    }

    public void setPointValues(List<PointValueTimeModel> pointValues) {
        this.pointValues = pointValues;
    }

    public long getEventActiveTime() {
        return eventActiveTime;
    }

    public void setEventActiveTime(long eventActiveTime) {
        this.eventActiveTime = eventActiveTime;
    }

}
