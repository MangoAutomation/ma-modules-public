/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

public class TimeRangePointValuesRequestModel extends PointValuesRequestModel {
    
    @DateTimeFormat(iso = ISO.DATE_TIME)
    protected DateTime from;
    
    @DateTimeFormat(iso = ISO.DATE_TIME)
    protected DateTime to;
    
    protected RollupEnum rollup = RollupEnum.NONE;
    protected TimePeriodType timePeriodType;
    protected Integer timePeriods;
 
    
    TimeRangePointValuesRequestModel() {
        this.limit = null;
    }
    
    public DateTime getFrom() {
        return from;
    }
    
    public void setFrom(DateTime from) {
        this.from = from;
    }

    public DateTime getTo() {
        return to;
    }

    public void setTo(DateTime to) {
        this.to = to;
    }
    public RollupEnum getRollup() {
        return rollup;
    }
    public void setRollup(RollupEnum rollup) {
        this.rollup = rollup;
    }
    public TimePeriodType getTimePeriodType() {
        return timePeriodType;
    }
    public void setTimePeriodType(TimePeriodType timePeriodType) {
        this.timePeriodType = timePeriodType;
    }
    public Integer getTimePeriods() {
        return timePeriods;
    }
    public void setTimePeriods(Integer timePeriods) {
        this.timePeriods = timePeriods;
    }

}
