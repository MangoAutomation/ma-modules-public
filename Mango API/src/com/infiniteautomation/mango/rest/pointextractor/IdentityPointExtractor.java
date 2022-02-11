/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.pointextractor;

import com.goebl.simplify.Point;
import com.goebl.simplify.PointExtractor;
import com.goebl.simplify.SimplifyUtility;

/**
 * Extracts X,Y coordinates from a {@link Point} so a series can be simplified using {@link SimplifyUtility}.
 *
 * @author Jared Wiltshire
 */
public class IdentityPointExtractor implements PointExtractor<Point> {

    public final static IdentityPointExtractor INSTANCE = new IdentityPointExtractor();

    private IdentityPointExtractor() {
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
