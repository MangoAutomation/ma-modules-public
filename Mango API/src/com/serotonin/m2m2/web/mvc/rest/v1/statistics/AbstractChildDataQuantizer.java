/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.statistics;

import org.joda.time.DateTime;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;

/**
 * @author Terry Packer
 *
 */
abstract public class AbstractChildDataQuantizer {

    private DataValue lastValue;

    public void start(DateTime start, DateTime end, DataValue value){
    	lastValue = value;
    	openPeriod(start, end, value);
    }
    
    public void done(DataValue endValue) {
        closePeriod(endValue);
    }

    void openPeriod(DateTime start, DateTime end){
    	this.openPeriod(start, end, this.lastValue);
    }
    
    void closePeriod(){
    	this.closePeriod(this.lastValue);
    }
    
    void addDataInPeriod(DataValue value, long time){
    	this.lastValue = value;
    	this.dataInPeriod(value, time);
    }
    
    /**
     * Tells the quantizer to open the period.
     * 
     * @param startValue
     *            the value that was current at the start of the period, i.e. the latest value that occurred
     *            before the period started. Can be null if the inception of the point occurred after or during this
     *            period. Will no be null even if the value occurred long before the start of the period.
     * @param start
     *            the start time (inclusive) of the period
     * @param end
     *            the end time (exclusive) of the period
     */
    abstract protected void openPeriod(DateTime start, DateTime end, DataValue startValue);

    /**
     * A value that occurred in the period. Data will be provided to this method in chronological order.
     * 
     * @param value
     * @param time
     */
    abstract protected void dataInPeriod(DataValue value, long time);

    /**
     * Tells the quantizer that there is no more data for the period.
     * 
     * @param done
     *            indicates that there will never be any more data given to any other
     */
    abstract protected void closePeriod(DataValue endValue);
}
