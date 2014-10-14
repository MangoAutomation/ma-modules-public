/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.view.quantize2.StatisticsGeneratorQuantizerCallback;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeJsonWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.RollupEnum;

/**
 * @author Terry Packer
 *
 */
public class NonNumericPointValueStatisticsQuantizerJsonCallback extends PointValueTimeJsonWriter implements StatisticsGeneratorQuantizerCallback<ValueChangeCounter>{

	private final Log LOG = LogFactory.getLog(NonNumericPointValueStatisticsQuantizerJsonCallback.class);
	private RollupEnum rollup;
	
	/**
	 * @param jgen
	 */
	public NonNumericPointValueStatisticsQuantizerJsonCallback(
			JsonGenerator jgen, RollupEnum rollup) {
		super(jgen);
		this.rollup = rollup;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.view.quantize2.StatisticsGeneratorQuantizerCallback#quantizedStatistics(com.serotonin.m2m2.view.stats.StatisticsGenerator, boolean)
	 */
	@Override
    public void quantizedStatistics(ValueChangeCounter statisticsGenerator, boolean done) {
		try{
	        if (statisticsGenerator.getCount() > 0 || !done) {
	            switch(rollup){
	            case FIRST:
	            	DataValue first = statisticsGenerator.getFirstValue();
	            	if(first != null)
	            		this.writePointValueTime(first, statisticsGenerator.getFirstTime(), null);
	            break;
	            case LAST:
	            	DataValue last = statisticsGenerator.getLastValue();
	            	if(last != null)
	            		this.writePointValueTime(last, statisticsGenerator.getLastTime(), null);
	            break;
	            case COUNT:
	            	this.writePointValueTime(statisticsGenerator.getCount(), statisticsGenerator.getPeriodEndTime() -1, null);
	            break;
	            default:
	            	throw new ShouldNeverHappenException("Unsupported Non-numerical Rollup type: " + rollup);
	       
	            }
	        }
		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
    }


}
