/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.event.detectors.rt;

import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeModel;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.event.detectors.RateOfChangeDetectorRT;
import com.serotonin.m2m2.vo.event.detector.RateOfChangeDetectorVO;

import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @author Terry Packer
 */
public class RateOfChangeEventDetectorRTModel extends TimeDelayedEventDetectorRTModel<RateOfChangeDetectorVO>{

    private PointValueTimeModel latestValue;
    private Double periodStartValue;
    private long periodStartTime;
    private double comparisonRoCPerMs;
    private double resetRoCPerMs;
    private long rocDurationMs;
    @ApiModelProperty("State field. Whether the RoC event is currently active or not. This field is used to prevent multiple events being raised during the duration of a single RoC threshold exceeded event.")
    private boolean rocBreachActive;
    private long rocBreachActiveTime;
    private long rocBreachInactiveTime;

    public RateOfChangeEventDetectorRTModel(RateOfChangeDetectorRT rt) {
        super(rt);
        PointValueTime latest = rt.getLatestValue();
        if(latest != null) {
            this.latestValue = new PointValueTimeModel(latest);
        }
        this.periodStartValue = rt.getPeriodStartValue();
        this.periodStartTime = rt.getPeriodStartTime();
        this.comparisonRoCPerMs = rt.getComparisonRoCPerMs();
        this.resetRoCPerMs = rt.getResetRoCPerMs();
        this.rocDurationMs = rt.getRocDurationMs();
        this.rocBreachActive = rt.isRocBreachActive();
        this.rocBreachActiveTime = rt.getRocBreachActiveTime();
        this.rocBreachInactiveTime = rt.getRocBreachInactiveTime();
    }

    public PointValueTimeModel getLatestValue() {
        return latestValue;
    }

    public void setLatestValue(PointValueTimeModel latestValue) {
        this.latestValue = latestValue;
    }

    public Double getPeriodStartValue() {
        return periodStartValue;
    }

    public void setPeriodStartValue(Double periodStartValue) {
        this.periodStartValue = periodStartValue;
    }

    public long getPeriodStartTime() {
        return periodStartTime;
    }

    public void setPeriodStartTime(long periodStartTime) {
        this.periodStartTime = periodStartTime;
    }

    public double getComparisonRoCPerMs() {
        return comparisonRoCPerMs;
    }

    public void setComparisonRoCPerMs(double comparisonRoCPerMs) {
        this.comparisonRoCPerMs = comparisonRoCPerMs;
    }

    public double getResetRoCPerMs() {
        return resetRoCPerMs;
    }

    public void setResetRoCPerMs(double resetRoCPerMs) {
        this.resetRoCPerMs = resetRoCPerMs;
    }

    public long getRocDurationMs() {
        return rocDurationMs;
    }

    public void setRocDurationMs(long rocDurationMs) {
        this.rocDurationMs = rocDurationMs;
    }

    public boolean isRocBreachActive() {
        return rocBreachActive;
    }

    public void setRocBreachActive(boolean rocBreachActive) {
        this.rocBreachActive = rocBreachActive;
    }

    public long getRocBreachActiveTime() {
        return rocBreachActiveTime;
    }

    public void setRocBreachActiveTime(long rocBreachActiveTime) {
        this.rocBreachActiveTime = rocBreachActiveTime;
    }

    public long getRocBreachInactiveTime() {
        return rocBreachInactiveTime;
    }

    public void setRocBreachInactiveTime(long rocBreachInactiveTime) {
        this.rocBreachInactiveTime = rocBreachInactiveTime;
    }

}
