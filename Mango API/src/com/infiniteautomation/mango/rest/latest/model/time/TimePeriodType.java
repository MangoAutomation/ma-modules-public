/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.time;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;


/**
 * @author Terry Packer
 *
 */
public enum TimePeriodType {

	MILLISECONDS(ChronoUnit.MILLIS),
	SECONDS(ChronoUnit.SECONDS),
	MINUTES(ChronoUnit.MINUTES),
	HOURS(ChronoUnit.HOURS),
	DAYS(ChronoUnit.DAYS),
	WEEKS(ChronoUnit.WEEKS),
	MONTHS(ChronoUnit.MONTHS),
	YEARS(ChronoUnit.YEARS);

	private final ChronoUnit chronoUnit;

	TimePeriodType(ChronoUnit chronoUnit) {
		this.chronoUnit = chronoUnit;
	}

	public ChronoUnit getChronoUnit() {
		return chronoUnit;
	}

	public TemporalAmount toTemporalAmount(int periods) {
		switch (chronoUnit) {
			case DAYS:
				return Period.ofDays(periods);
			case WEEKS:
				return Period.ofWeeks(periods);
			case MONTHS:
				return Period.ofMonths(periods);
			case YEARS:
				return Period.ofYears(periods);
			default:
				return Duration.of(periods, chronoUnit);
		}
	}

	/**
	 * Convert Mango's Common.TimePeriods to the Enum Type TimePeriodType
	 * @see Common.TimePeriods
	 *
     */
	public static TimePeriodType convertTo(int updatePeriodType) {
		switch(updatePeriodType){
		case Common.TimePeriods.MILLISECONDS:
			return MILLISECONDS;
		case Common.TimePeriods.SECONDS:
			return SECONDS; 
		case Common.TimePeriods.MINUTES:
			return MINUTES;
		case Common.TimePeriods.HOURS:
			return HOURS;
		case Common.TimePeriods.DAYS:
			return DAYS;
		case Common.TimePeriods.WEEKS:
			return WEEKS;
		case Common.TimePeriods.MONTHS:
			return MONTHS;
		case Common.TimePeriods.YEARS:
			return YEARS;
		default:
			throw new ShouldNeverHappenException("No Time Period Type exists for value :" + updatePeriodType);
		}
	}
	
	/**
	 * Convert this enum into Common.TimePeriods
	 * @see Common.TimePeriods
	 *
     */
	public static int convertFrom(TimePeriodType type) {
		switch(type){
		case MILLISECONDS:
			return Common.TimePeriods.MILLISECONDS;
		case SECONDS:
			return Common.TimePeriods.SECONDS; 
		case MINUTES:
			return Common.TimePeriods.MINUTES;
		case HOURS:
			return Common.TimePeriods.HOURS;
		case DAYS:
			return Common.TimePeriods.DAYS;
		case WEEKS:
			return Common.TimePeriods.WEEKS;
		case MONTHS:
			return Common.TimePeriods.MONTHS;
		case YEARS:
			return Common.TimePeriods.YEARS;
		default:
			throw new ShouldNeverHappenException("No Common.TimePeriods value exists for type :" + type);
		}
	}
}
