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
import com.serotonin.m2m2.view.quantize2.StatisticsGeneratorQuantizerCallback;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeJsonWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.RollupEnum;

/**
 * @author Terry Packer
 *
 */
public class NumericPointValueStatisticsQuantizerJsonCallback extends PointValueTimeJsonWriter implements StatisticsGeneratorQuantizerCallback<AnalogStatistics>{

	private final Log LOG = LogFactory.getLog(NumericPointValueStatisticsQuantizerJsonCallback.class);
	private RollupEnum rollup;
	
	/**
	 * @param jgen
	 */
	public NumericPointValueStatisticsQuantizerJsonCallback(JsonGenerator jgen, RollupEnum rollup) {
		super(jgen);
		this.rollup = rollup;
	}	
	
	
	@Override
    public void quantizedStatistics(AnalogStatistics statisticsGenerator, boolean done) {

		try{
	        if (statisticsGenerator.getCount() > 0 || !done) {
	            switch(rollup){
	                case AVERAGE:
	                	Double avg = statisticsGenerator.getAverage();
	                	if(avg == null)
	                		avg = 0.0D;
						this.writePointValueTime(avg, statisticsGenerator.getPeriodEndTime() - 1, null);
	                break;
	                case MINIMUM:
	                	Double min = statisticsGenerator.getMinimumValue();
	                	if(min != null)
	                		this.writePointValueTime(min, statisticsGenerator.getMinimumTime(), null);
	                break;
	                case MAXIMUM:
	                	Double max = statisticsGenerator.getMaximumValue();
	                	if(max != null)
	                		this.writePointValueTime(max, statisticsGenerator.getMaximumTime(), null);
	                break;
	                case SUM:
	                	Double sum = statisticsGenerator.getSum();
	                	if(sum == null)
	                		sum = 0.0D;
                		this.writePointValueTime(sum, statisticsGenerator.getPeriodEndTime() - 1, null);
	                break;
	                case FIRST:
	                	Double first = statisticsGenerator.getFirstValue();
	                	if(first != null)
	                		this.writePointValueTime(first, statisticsGenerator.getFirstTime(), null);
	                break;
	                case LAST:
	                	Double last = statisticsGenerator.getLastValue();
	                	if(last != null)
	                		this.writePointValueTime(last, statisticsGenerator.getLastTime(), null);
	                break;
	                case COUNT:
	                	this.writePointValueTime(statisticsGenerator.getCount(),
	                			statisticsGenerator.getPeriodEndTime() - 1, null);
	                break;
	                default:
	                	throw new ShouldNeverHappenException("Unknown Rollup type" + rollup);
	            }
	        }
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
    }
	
	
	
}
