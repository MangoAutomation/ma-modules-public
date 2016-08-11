/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.statistics;

import org.joda.time.DateTime;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;

/**
 * @author Terry Packer
 *
 */
public class ValueChangeCounterChildQuantizer extends AbstractChildDataQuantizer {

	private final int id;
    private final ChildStatisticsQuantizerCallback callback;
    private ValueChangeCounter valueChangeCounter;

    public ValueChangeCounterChildQuantizer(int id, ChildStatisticsQuantizerCallback callback) {
        this.id = id;
    	this.callback = callback;
    }

    @Override
    protected void openPeriod(DateTime start, DateTime end, DataValue startValue) {
        valueChangeCounter = new ValueChangeCounter(start.getMillis(), end.getMillis(), startValue);
    }

    @Override
    protected void dataInPeriod(DataValue value, long time) {
        valueChangeCounter.addValue(value, time);
    }

    @Override
    protected void closePeriod(DataValue endValue) {
        if (valueChangeCounter != null) {
            valueChangeCounter.done();
            callback.quantizedStatistics(id, valueChangeCounter, endValue == null);
        }
    }

}
