/*
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.dataPoint;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.DataPointVO;


/**
 * @author Jared Wiltshire
 */
public class LoggingPropertiesModel {

    private String loggingType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String intervalLoggingType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TimePeriodModel intervalLoggingPeriod;
    private Double tolerance;
    private Boolean discardExtremeValues;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double discardLowLimit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double discardHighLimit;
    private Boolean overrideIntervalLoggingSamples;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer intervalLoggingSampleWindowSize;
    private Integer cacheSize;

    public LoggingPropertiesModel() {
    }

    public LoggingPropertiesModel(DataPointVO point) {
        this.loggingType = DataPointVO.LOGGING_TYPE_CODES.getCode(point.getLoggingType());
        if (point.getLoggingType() == DataPointVO.LoggingTypes.INTERVAL) {
            this.intervalLoggingType = DataPointVO.INTERVAL_LOGGING_TYPE_CODES.getCode(point.getIntervalLoggingType());
            this.intervalLoggingPeriod = new TimePeriodModel(point.getIntervalLoggingPeriod(), point.getIntervalLoggingPeriodType());
        } else if (point.getLoggingType() == DataPointVO.LoggingTypes.ON_CHANGE_INTERVAL) {
            this.intervalLoggingPeriod = new TimePeriodModel(point.getIntervalLoggingPeriod(), point.getIntervalLoggingPeriodType());
        }

        this.tolerance = point.getTolerance();

        this.discardExtremeValues = point.isDiscardExtremeValues();
        if (this.discardExtremeValues) {
            this.discardLowLimit = point.getDiscardLowLimit();
            this.discardHighLimit = point.getDiscardHighLimit();
        }

        this.overrideIntervalLoggingSamples = point.isOverrideIntervalLoggingSamples();
        if (this.overrideIntervalLoggingSamples) {
            this.intervalLoggingSampleWindowSize = point.getIntervalLoggingSampleWindowSize();
        }

        this.cacheSize = point.getDefaultCacheSize();
    }

    public void copyPropertiesTo(DataPointVO point) {
        if (this.loggingType != null) {
            point.setLoggingType(DataPointVO.LOGGING_TYPE_CODES.getId(this.loggingType));
        }
        if (this.intervalLoggingType != null) {
            point.setIntervalLoggingType(DataPointVO.INTERVAL_LOGGING_TYPE_CODES.getId(this.intervalLoggingType));
        }
        if (this.intervalLoggingPeriod != null) {
            point.setIntervalLoggingPeriod(this.intervalLoggingPeriod.getPeriods());
            point.setIntervalLoggingPeriodType(Common.TIME_PERIOD_CODES.getId(this.intervalLoggingPeriod.getPeriodType()));
        }
        if (this.tolerance != null) {
            point.setTolerance(this.tolerance);
        }
        if (this.discardExtremeValues != null) {
            point.setDiscardExtremeValues(this.discardExtremeValues);
        }
        if (this.discardLowLimit != null) {
            point.setDiscardLowLimit(this.discardLowLimit);
        }
        if (this.discardHighLimit != null) {
            point.setDiscardHighLimit(this.discardHighLimit);
        }
        if (this.overrideIntervalLoggingSamples != null) {
            point.setOverrideIntervalLoggingSamples(this.overrideIntervalLoggingSamples);
        }
        if (this.intervalLoggingSampleWindowSize != null) {
            point.setIntervalLoggingSampleWindowSize(this.intervalLoggingSampleWindowSize);
        }
        if (this.cacheSize != null) {
            point.setDefaultCacheSize(this.cacheSize);
        }
    }

    public String getLoggingType() {
        return loggingType;
    }

    public void setLoggingType(String loggingType) {
        this.loggingType = loggingType;
    }

    public String getIntervalLoggingType() {
        return intervalLoggingType;
    }

    public void setIntervalLoggingType(String intervalLoggingType) {
        this.intervalLoggingType = intervalLoggingType;
    }

    public TimePeriodModel getIntervalLoggingPeriod() {
        return intervalLoggingPeriod;
    }

    public void setIntervalLoggingPeriod(TimePeriodModel intervalLoggingPeriod) {
        this.intervalLoggingPeriod = intervalLoggingPeriod;
    }

    public Double getTolerance() {
        return tolerance;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
    }

    public Boolean getDiscardExtremeValues() {
        return discardExtremeValues;
    }

    public void setDiscardExtremeValues(Boolean discardExtremeValues) {
        this.discardExtremeValues = discardExtremeValues;
    }

    public Double getDiscardLowLimit() {
        return discardLowLimit;
    }

    public void setDiscardLowLimit(Double discardLowLimit) {
        this.discardLowLimit = discardLowLimit;
    }

    public Double getDiscardHighLimit() {
        return discardHighLimit;
    }

    public void setDiscardHighLimit(Double discardHighLimit) {
        this.discardHighLimit = discardHighLimit;
    }

    public Boolean getOverrideIntervalLoggingSamples() {
        return overrideIntervalLoggingSamples;
    }

    public void setOverrideIntervalLoggingSamples(Boolean overrideIntervalLoggingSamples) {
        this.overrideIntervalLoggingSamples = overrideIntervalLoggingSamples;
    }

    public Integer getIntervalLoggingSampleWindowSize() {
        return intervalLoggingSampleWindowSize;
    }

    public void setIntervalLoggingSampleWindowSize(Integer intervalLoggingSampleWindowSize) {
        this.intervalLoggingSampleWindowSize = intervalLoggingSampleWindowSize;
    }

    public Integer getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Integer cacheSize) {
        this.cacheSize = cacheSize;
    }
}
