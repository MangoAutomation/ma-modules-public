/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.statistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.joda.time.DateTime;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.quantize2.BucketCalculator;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;

/**
 * Allows quantization for many quantizers at the same time
 * 
 * @author Terry Packer
 *
 */
public class ParentDataQuantizer implements ChildStatisticsQuantizerCallback{
	
	protected Map<Integer, AbstractChildDataQuantizer> quantizers;
	//Map of current statistics for this period
	protected Map<Integer, StatisticsGenerator> periodStatsMap;
	protected BucketCalculator bucketCalculator;
	protected ParentStatisticsQuantizerCallback callback;
	
    protected final long startTime;
    protected DateTime periodFrom;
    protected DateTime periodTo;
	
	
    public ParentDataQuantizer(BucketCalculator bucketCalculator, ParentStatisticsQuantizerCallback callback) {
        this.quantizers = new HashMap<Integer, AbstractChildDataQuantizer>();
        this.bucketCalculator = bucketCalculator;
        this.callback = callback;
        this.periodFrom = bucketCalculator.getStartTime();
        this.periodTo = bucketCalculator.getNextPeriodTo();
        this.startTime = this.periodFrom.getMillis();
        this.periodStatsMap = new HashMap<Integer, StatisticsGenerator>();
    }

    /**
     * Add a quantizer
     * @param id
     * @param quantizer
     */
    public void startQuantizer(Integer dataPointId, DataValue startValue, AbstractChildDataQuantizer quantizer){
    	this.quantizers.put(dataPointId, quantizer);
    	quantizer.start(this.periodFrom, this.periodTo, startValue);
    }
    
    /**
     * Next value in time ascending order
     * @param value
     */
    public void data(Integer dataPointId, DataValue value, long time){        
    	if (time < this.startTime)
            throw new IllegalArgumentException("Data is before start time");

        if (time >= this.bucketCalculator.getEndTime().getMillis())
            throw new IllegalArgumentException("Data is after end time");

        //Advance our buckets and fill if we are past our period
        while (time >= this.periodTo.getMillis()){
        	long periodStartTime = this.periodFrom.getMillis();
        	this.periodFrom = this.periodTo;
        	this.periodTo = this.bucketCalculator.getNextPeriodTo();

        	for(AbstractChildDataQuantizer q : this.quantizers.values()){
        		q.closePeriod();
        		q.openPeriod(this.periodFrom, this.periodTo);
        	}
        	this.callback.closePeriod(this.periodStatsMap, periodStartTime);
        	this.periodStatsMap.clear();
        }
        
        //Update the child quantizer
    	AbstractChildDataQuantizer quantizer = this.quantizers.get(dataPointId);
    	if(quantizer != null){
            quantizer.addDataInPeriod(value, time);
    	}
    }

    /**
     * 
     * @param endValues - Map of data point to last value, null if DNE. Map must contain an entry for all data points used.
     */
    public void done(Map<Integer, DataValue> endValues) {

    	if(endValues.size() != this.quantizers.size())
    		throw new ShouldNeverHappenException("Invalid number of endValues, must be one for every data point quantizer.");
    	
        //Finish to our end time
        while (periodTo.isBefore(bucketCalculator.getEndTime())){
        	long periodStartTime = this.periodFrom.getMillis();
        	this.periodFrom = this.periodTo;
        	this.periodTo = this.bucketCalculator.getNextPeriodTo();

        	for(AbstractChildDataQuantizer q : this.quantizers.values()){
        		q.closePeriod();
        		q.openPeriod(this.periodFrom, this.periodTo);
        	}
        	this.callback.closePeriod(this.periodStatsMap, periodStartTime);
        	this.periodStatsMap.clear();
        }

        //Send in the last values
        Iterator<Integer> it = endValues.keySet().iterator();
    	while(it.hasNext()){
    		Integer pointId = it.next();
	        //Close 
	    	AbstractChildDataQuantizer quantizer = this.quantizers.get(pointId);
	    	if(quantizer != null){
	    		quantizer.done(endValues.get(pointId));
	    	}
    	}

    	this.callback.closePeriod(this.periodStatsMap, this.periodFrom.getMillis());
    	this.periodStatsMap.clear();
    }

	/**
	 * Children callback to us to keep the period values at the end of a period
	 * 
	 * @param dataPointId
	 * @param statisticsGenerator
	 * @param done
	 */
    @Override
	public void quantizedStatistics(int dataPointId, StatisticsGenerator stats, boolean done) {
    	this.periodStatsMap.put(dataPointId, stats);
	}

    
    
}
