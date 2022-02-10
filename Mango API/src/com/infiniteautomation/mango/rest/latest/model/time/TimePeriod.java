/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.time;

import java.time.temporal.TemporalAmount;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Terry Packer
 *
 */
public class TimePeriod {
	
	public TimePeriod(){
	
	}
	
	public TimePeriod(int periods, TimePeriodType type){
		this.periods = periods;
		this.type = type;
	}
	
	@JsonProperty
	private int periods;
	
	@JsonProperty
	private TimePeriodType type;

	public int getPeriods() {
		return periods;
	}

	public void setPeriods(int periods) {
		this.periods = periods;
	}

	public TimePeriodType getType() {
		return type;
	}

	public void setType(TimePeriodType type) {
		this.type = type;
	}

	public TemporalAmount toTemporalAmount() {
		return type.toTemporalAmount(periods);
	}
	
}
