/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue.streams;

import com.goebl.simplify.PointExtractor;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;

/**
 * @author Jared Wiltshire
 */
public class IdPointValueTimePointExtractor implements PointExtractor<IdPointValueTime> {

    public final static IdPointValueTimePointExtractor INSTANCE = new IdPointValueTimePointExtractor();

    private IdPointValueTimePointExtractor() {
    }

    @Override
    public double getX(IdPointValueTime point) {
        return point.getTime();
    }

    @Override
    public double getY(IdPointValueTime point) {
        return point.getDoubleValue();
    }
}
