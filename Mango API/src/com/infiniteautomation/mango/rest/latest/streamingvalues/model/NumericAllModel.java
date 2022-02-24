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

    ValueModel accumulator;
    ValueModel average;
    ValueModel delta;
    ValueModel integral;
    ValueModel maximum;
    ValueModel minimum;
    ValueModel sum;
    ValueModel maximumInPeriod;
    ValueModel minimumInPeriod;
    ValueModel arithmeticMean;

    public ValueModel getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(ValueModel accumulator) {
        this.accumulator = accumulator;
    }

    public ValueModel getAverage() {
        return average;
    }

    public void setAverage(ValueModel average) {
        this.average = average;
    }

    public ValueModel getDelta() {
        return delta;
    }

    public void setDelta(ValueModel delta) {
        this.delta = delta;
    }

    public ValueModel getIntegral() {
        return integral;
    }

    public void setIntegral(ValueModel integral) {
        this.integral = integral;
    }

    public ValueModel getMaximum() {
        return maximum;
    }

    public void setMaximum(ValueModel maximum) {
        this.maximum = maximum;
    }

    public ValueModel getMinimum() {
        return minimum;
    }

    public void setMinimum(ValueModel minimum) {
        this.minimum = minimum;
    }

    public ValueModel getSum() {
        return sum;
    }

    public void setSum(ValueModel sum) {
        this.sum = sum;
    }

    public ValueModel getMaximumInPeriod() {
        return maximumInPeriod;
    }

    public void setMaximumInPeriod(ValueModel maximumInPeriod) {
        this.maximumInPeriod = maximumInPeriod;
    }

    public ValueModel getMinimumInPeriod() {
        return minimumInPeriod;
    }

    public void setMinimumInPeriod(ValueModel minimumInPeriod) {
        this.minimumInPeriod = minimumInPeriod;
    }

    public ValueModel getArithmeticMean() {
        return arithmeticMean;
    }

    public void setArithmeticMean(ValueModel arithmeticMean) {
        this.arithmeticMean = arithmeticMean;
    }
}
