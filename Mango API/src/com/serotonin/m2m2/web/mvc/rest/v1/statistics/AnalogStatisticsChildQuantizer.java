/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.statistics;

import org.joda.time.DateTime;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.AnalogStatistics;

/**
 * @author Terry Packer
 *
 */
public class AnalogStatisticsChildQuantizer extends AbstractChildDataQuantizer {

	private final int id;
    private final ChildStatisticsQuantizerCallback callback;
    private AnalogStatistics analogStatistics;

    public AnalogStatisticsChildQuantizer(int id, ChildStatisticsQuantizerCallback callback) {
        this.id = id;
    	this.callback = callback;
    }

    @Override
    protected void openPeriod(DateTime start, DateTime end, DataValue startValue) {
        analogStatistics = new AnalogStatistics(start.getMillis(), end.getMillis(), startValue == null ? null
                : startValue.getDoubleValue());
    }

    @Override
    protected void dataInPeriod(DataValue value, long time) {
        analogStatistics.addValueTime(value, time);
    }

    @Override
    protected void closePeriod(DataValue endValue) {
        if (analogStatistics != null) {
            analogStatistics.done(endValue == null ? null : endValue.getDoubleValue());
            callback.quantizedStatistics(id, analogStatistics, endValue == null);
        }
    }
}
