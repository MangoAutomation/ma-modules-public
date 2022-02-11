/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.pointextractor;

import com.goebl.simplify.PointExtractor;
import com.goebl.simplify.SimplifyUtility;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;

/**
 * Extracts X,Y coordinates from a {@link PointValueTime} so a series can be simplified using {@link SimplifyUtility}.
 *
 * @author Jared Wiltshire
 */
public class PointValueTimePointExtractor implements PointExtractor<PointValueTime> {

    public final static PointValueTimePointExtractor INSTANCE = new PointValueTimePointExtractor();

    private PointValueTimePointExtractor() {
    }

    @Override
    public double getX(PointValueTime point) {
        return point.getTime();
    }

    @Override
    public double getY(PointValueTime point) {
        return point.getDoubleValue();
    }
}
