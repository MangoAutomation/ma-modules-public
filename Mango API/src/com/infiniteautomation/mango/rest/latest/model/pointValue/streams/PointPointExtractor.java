/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.pointValue.streams;

import com.goebl.simplify.Point;
import com.goebl.simplify.PointExtractor;

/**
 * @author Jared Wiltshire
 */
public class PointPointExtractor implements PointExtractor<Point> {

    public final static PointPointExtractor INSTANCE = new PointPointExtractor();

    private PointPointExtractor() {
    }

    @Override
    public double getX(Point point) {
        return point.getX();
    }

    @Override
    public double getY(Point point) {
        return point.getY();
    }

}
