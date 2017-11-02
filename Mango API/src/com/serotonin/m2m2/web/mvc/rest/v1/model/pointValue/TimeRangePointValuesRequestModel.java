package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import org.joda.time.DateTime;

import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.TimePeriodType;

public class TimeRangePointValuesRequestModel extends PointValuesRequestModel {
    private DateTime from;
    private DateTime to;
    private RollupEnum rollup = RollupEnum.NONE;
    private TimePeriodType timePeriodType;
    private Integer timePeriods;
    private String timezone;
    private Integer limit; //shadow super.limit
    
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
    public String getTimezone() {
        return timezone;
    }
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    @Override
    public Integer getLimit() {
        return limit;
    }
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
