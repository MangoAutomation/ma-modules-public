package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.util.Date;

import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

public class TimeRangePointValuesRequestModel extends PointValuesRequestModel {
    Date from;
    Date to;
    RollupEnum rollup = RollupEnum.NONE;
    TimePeriodType timePeriodType;
    Integer timePeriods;
    String timezone;
    
    TimeRangePointValuesRequestModel() {
        limit = null;
    }
    
    public Date getFrom() {
        return from;
    }
    public void setFrom(Date from) {
        this.from = from;
    }
    public Date getTo() {
        return to;
    }
    public void setTo(Date to) {
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
    public String getTimezone() {
        return timezone;
    }
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
