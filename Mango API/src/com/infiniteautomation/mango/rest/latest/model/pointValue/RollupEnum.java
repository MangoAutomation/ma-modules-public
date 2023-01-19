/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;

/**
 * @author Terry Packer
 */
public enum RollupEnum {
    NONE(true, Common.Rollups.NONE),
    AVERAGE(false, Common.Rollups.AVERAGE),
    DELTA(false, Common.Rollups.DELTA),
    MINIMUM(false, Common.Rollups.MINIMUM),
    MAXIMUM(false, Common.Rollups.MAXIMUM),
    ACCUMULATOR(false, Common.Rollups.ACCUMULATOR),
    SUM(false, Common.Rollups.SUM),
    FIRST(true, Common.Rollups.FIRST),
    LAST(true, Common.Rollups.LAST),
    COUNT(true, Common.Rollups.COUNT),
    INTEGRAL(false, Common.Rollups.INTEGRAL),
    FFT(false, -1),
    ALL(true, Common.Rollups.ALL),
    START(true, Common.Rollups.START),
    POINT_DEFAULT(true, -2),
    ARITHMETIC_MEAN(false, Common.Rollups.ARITHMETIC_MEAN),
    MINIMUM_IN_PERIOD(false, Common.Rollups.MINIMUM_IN_PERIOD),
    MAXIMUM_IN_PERIOD(false, Common.Rollups.MAXIMUM_IN_PERIOD),
    RANGE_IN_PERIOD(false, Common.Rollups.RANGE_IN_PERIOD);

    private final boolean nonNumericSupport; //Does this rollup support Non-Numeric point values
    private final int id;

    RollupEnum(boolean nonNumericSupport, int id) {
        this.nonNumericSupport = nonNumericSupport;
        this.id = id;
    }

    public static RollupEnum convertTo(int id) {
        for (RollupEnum r : RollupEnum.values())
            if (r.id == id)
                return r;

        throw new ShouldNeverHappenException("Unknown Rollup, id: " + id);
    }

    /**
     * Convert from an ENUM String to an ID
     * if none is found return -1
     */
    public static int getFromCode(String code) {
        for (RollupEnum r : RollupEnum.values())
            if (r.name().equals(code))
                return r.id;
        return -1;
    }

    public boolean nonNumericSupport() {
        return this.nonNumericSupport;
    }

    public int getId() {
        return this.id;
    }
}
