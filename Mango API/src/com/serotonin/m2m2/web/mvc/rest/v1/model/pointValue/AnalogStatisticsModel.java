/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public class AnalogStatisticsModel extends PointStatisticsModel{
	
	

	@JsonProperty
	private PointValueTimeModel minimum;
	@JsonProperty
    private PointValueTimeModel maximum;
	@JsonProperty
    private Double average;
	@JsonProperty
    private Double integral;
	@JsonProperty
    private double sum;
	@JsonProperty
	private PointValueTimeModel first;
	@JsonProperty
	private PointValueTimeModel last;
	@JsonProperty
    private int count;
	
	@Override
	@JsonGetter("message")
	public String getMessage(){
		return "Analog Statistics";
	}
	

	public Double getAverage() {
		return average;
	}
	public void setAverage(Double average) {
		this.average = average;
	}
	public Double getIntegral() {
		return integral;
	}
	public void setIntegral(Double integral) {
		this.integral = integral;
	}
	public double getSum() {
		return sum;
	}
	public void setSum(double sum) {
		this.sum = sum;
	}

	public PointValueTimeModel getMinimum() {
		return minimum;
	}


	public void setMinimum(PointValueTimeModel minimum) {
		this.minimum = minimum;
	}


	public PointValueTimeModel getMaximum() {
		return maximum;
	}


	public void setMaximum(PointValueTimeModel maximum) {
		this.maximum = maximum;
	}


	public PointValueTimeModel getFirst() {
		return first;
	}


	public void setFirst(PointValueTimeModel first) {
		this.first = first;
	}


	public PointValueTimeModel getLast() {
		return last;
	}


	public void setLast(PointValueTimeModel last) {
		this.last = last;
	}


	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	
}
