/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors;

import com.infiniteautomation.mango.rest.latest.model.time.TimePeriod;
import com.infiniteautomation.mango.rest.latest.model.time.TimePeriodType;
import com.serotonin.m2m2.vo.event.detector.TimeoutDetectorVO;

/**
 *
 * @author Terry Packer
 */
public abstract class TimeoutDetectorModel<T extends TimeoutDetectorVO<T>> extends AbstractPointEventDetectorModel<T>{

    private TimePeriod duration;

    public TimeoutDetectorModel(T data) {
        fromVO(data);
    }

    public TimeoutDetectorModel() {

    }

    @Override
    public void fromVO(T vo) {
        super.fromVO(vo);
        this.duration = new TimePeriod(vo.getDuration(),
                TimePeriodType.convertTo(vo.getDurationType()));
    }

    @Override
    public T toVO() {
        T vo = super.toVO();
        if(duration != null) {
            vo.setDuration(duration.getPeriods());
            vo.setDurationType(TimePeriodType.convertFrom(duration.getType()));
        }else {
            vo.setDuration(-1);
            vo.setDurationType(-1);
        }
        return vo;
    }

    /**
     * @return the duration
     */
    public TimePeriod getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(TimePeriod duration) {
        this.duration = duration;
    }

}
