/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.time.ZonedDateTime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

public class TimeRangePointValuesRequestModel extends PointValuesRequestModel {
    
    @DateTimeFormat(iso = ISO.DATE_TIME)
    protected ZonedDateTime from;
    
    @DateTimeFormat(iso = ISO.DATE_TIME)
    protected ZonedDateTime to;
    
    protected RollupEnum rollup = RollupEnum.NONE;
    protected TimePeriodType timePeriodType;
    protected Integer timePeriods;
 
    
    TimeRangePointValuesRequestModel() {
        this.limit = null;
    }
    
    public ZonedDateTime getFrom() {
        return from;
    }
    
    public void setFrom(ZonedDateTime from) {
        this.from = from;
    }

    @JsonIgnore
    public DateTime getFromAsDateTime() {
       if(from == null)
           return null;
       else
           return new DateTime(from.toInstant().toEpochMilli(), DateTimeZone.forTimeZone(java.util.TimeZone.getTimeZone(from.getZone())));
    }
    public ZonedDateTime getTo() {
        return to;
    }

    public void setTo(ZonedDateTime to) {
        this.to = to;
    }
    
    @JsonIgnore
    public DateTime getToAsDateTime() {
        if(to == null)
            return null;
        else
            return new DateTime(to.toInstant().toEpochMilli(), DateTimeZone.forTimeZone(java.util.TimeZone.getTimeZone(to.getZone())));
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
