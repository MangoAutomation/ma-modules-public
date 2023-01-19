/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.model;

/**
 * Used for data points with NUMERIC data type.
 *
 * @author Jared Wiltshire
 */
public class NumericAllModel extends AllStatisticsModel {

    ValueTimeModel accumulator;
    ValueTimeModel average;
    ValueTimeModel delta;
    ValueTimeModel integral;
    ValueTimeModel maximum;
    ValueTimeModel minimum;
    ValueTimeModel sum;
    ValueTimeModel maximumInPeriod;
    ValueTimeModel minimumInPeriod;
    ValueTimeModel arithmeticMean;
    ValueTimeModel rangeInPeriod;

    public ValueTimeModel getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(ValueTimeModel accumulator) {
        this.accumulator = accumulator;
    }

    public ValueTimeModel getAverage() {
        return average;
    }

    public void setAverage(ValueTimeModel average) {
        this.average = average;
    }

    public ValueTimeModel getDelta() {
        return delta;
    }

    public void setDelta(ValueTimeModel delta) {
        this.delta = delta;
    }

    public ValueTimeModel getIntegral() {
        return integral;
    }

    public void setIntegral(ValueTimeModel integral) {
        this.integral = integral;
    }

    public ValueTimeModel getMaximum() {
        return maximum;
    }

    public void setMaximum(ValueTimeModel maximum) {
        this.maximum = maximum;
    }

    public ValueTimeModel getMinimum() {
        return minimum;
    }

    public void setMinimum(ValueTimeModel minimum) {
        this.minimum = minimum;
    }

    public ValueTimeModel getSum() {
        return sum;
    }

    public void setSum(ValueTimeModel sum) {
        this.sum = sum;
    }

    public ValueTimeModel getMaximumInPeriod() {
        return maximumInPeriod;
    }

    public void setMaximumInPeriod(ValueTimeModel maximumInPeriod) {
        this.maximumInPeriod = maximumInPeriod;
    }

    public ValueTimeModel getMinimumInPeriod() {
        return minimumInPeriod;
    }

    public void setMinimumInPeriod(ValueTimeModel minimumInPeriod) {
        this.minimumInPeriod = minimumInPeriod;
    }

    public ValueTimeModel getArithmeticMean() {
        return arithmeticMean;
    }

    public void setArithmeticMean(ValueTimeModel arithmeticMean) {
        this.arithmeticMean = arithmeticMean;
    }

    public ValueTimeModel getRangeInPeriod() {
        return rangeInPeriod;
    }

    public void setRangeInPeriod(ValueTimeModel rangeInPeriod) {
        this.rangeInPeriod = rangeInPeriod;
    }
}
