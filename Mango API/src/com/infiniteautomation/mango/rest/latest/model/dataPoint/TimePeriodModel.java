/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.dataPoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.serotonin.m2m2.Common;

/**
 * @author Terry Packer
 *
 */
public class TimePeriodModel {

    @JsonProperty("periods")
    private int periods;
    @JsonProperty("type")
    private String periodType;

    public TimePeriodModel(){ }

    public TimePeriodModel(int periods, int periodTypeId){
        this.periods = periods;
        this.periodType = Common.TIME_PERIOD_CODES.getCode(periodTypeId);
    }

    public TimePeriodModel(int periods, String periodType){
        this.periods = periods;
        this.periodType = periodType;
    }

    public int getPeriods() {
        return periods;
    }

    public void setPeriods(int periods) {
        this.periods = periods;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }


}
