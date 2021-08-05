/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.virtual.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.virtual.vo.AnalogAttractorChangeVO;

public class AnalogAttractorChangeRT extends ChangeTypeRT {
    private static Logger log = LoggerFactory.getLogger(AnalogAttractorChangeRT.class);

    private final AnalogAttractorChangeVO vo;

    public AnalogAttractorChangeRT(AnalogAttractorChangeVO vo) {
        this.vo = vo;
    }

    @Override
    public DataValue change(DataValue currentValue) {
        double current = currentValue.getDoubleValue();

        // Get the value we're attracted to.
        DataPointRT point = Common.runtimeManager.getDataPoint(vo.getAttractionPointId());
        if (point == null) {
            if (log.isDebugEnabled())
                log.debug("Attraction point is not enabled");
            // Point is not currently active.
            return new NumericValue(current);
        }

        DataValue attractorValue = PointValueTime.getValue(point.getPointValue());
        if (attractorValue == null) {
            if (log.isDebugEnabled())
                log.debug("Attraction point has not vaue");
            return new NumericValue(current);
        }

        double attraction = attractorValue.getDoubleValue();

        // Move half the distance toward the attractor...
        double change = (attraction - current) / 2;

        // ... subject to the maximum change allowed...
        if (change < 0 && -change > vo.getMaxChange())
            change = -vo.getMaxChange();
        else if (change > vo.getMaxChange())
            change = vo.getMaxChange();

        // ... and a random fluctuation.
        change += RANDOM.nextDouble() * vo.getVolatility() * 2 - vo.getVolatility();

        if (log.isDebugEnabled())
            log.debug("attraction=" + attraction + ", change=" + change);

        return new NumericValue(current + change);
    }
}
