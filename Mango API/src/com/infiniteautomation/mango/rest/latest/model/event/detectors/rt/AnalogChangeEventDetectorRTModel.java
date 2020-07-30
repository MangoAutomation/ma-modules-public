/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.model.event.detectors.rt;

import java.util.ArrayList;
import java.util.List;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.event.detectors.AnalogChangeDetectorRT;
import com.serotonin.m2m2.vo.event.detector.AnalogChangeDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class AnalogChangeEventDetectorRTModel extends TimeoutDetectorRTModel<AnalogChangeDetectorVO>{

    private PointValueTimeModel instantValue;
    private List<PointValueTimeModel> periodValues;
    private PointValueTimeModel max;
    private PointValueTimeModel min;
    @ApiModelProperty("Was a value backdated, meaning the detector will need to sort the next during its execution")
    private boolean dirty;

    public AnalogChangeEventDetectorRTModel(AnalogChangeDetectorRT rt) {
        super(rt);
        PointValueTime instant = rt.getInstantValue();
        if(instant != null) {
            this.instantValue = new PointValueTimeModel(instant);
        }
        List<PointValueTime> period = rt.getPeriodValues();
        this.periodValues = new ArrayList<>();
        for(PointValueTime pvt : period) {
            this.periodValues.add(new PointValueTimeModel(pvt));
        }
        this.min = new PointValueTimeModel(new PointValueTime(rt.getMin(), rt.getMinTime()));
        this.max = new PointValueTimeModel(new PointValueTime(rt.getMax(), rt.getMaxTime()));
        this.dirty = rt.isDirty();
    }

    public PointValueTimeModel getInstantValue() {
        return instantValue;
    }

    public void setInstantValue(PointValueTimeModel instantValue) {
        this.instantValue = instantValue;
    }

    public List<PointValueTimeModel> getPeriodValues() {
        return periodValues;
    }

    public void setPeriodValues(List<PointValueTimeModel> periodValues) {
        this.periodValues = periodValues;
    }

    public PointValueTimeModel getMax() {
        return max;
    }

    public void setMax(PointValueTimeModel max) {
        this.max = max;
    }

    public PointValueTimeModel getMin() {
        return min;
    }

    public void setMin(PointValueTimeModel min) {
        this.min = min;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

}
