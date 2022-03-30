/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.mapper;

/**
 * @author Jared Wiltshire
 */
public enum TimestampSource {
    /**
     * Timestamp of value/time model should be set to the start time of the rollup period.
     */
    PERIOD_START_TIME,

    /**
     * Timestamp of value/time model should be set to the time corresponding to the FIRST/LAST/MAXIMUM/MINIMUM value in the period.
     * For all other rollup/statistic types the timestamp is set to the start time of the rollup period.
     */
    STATISTIC
}
