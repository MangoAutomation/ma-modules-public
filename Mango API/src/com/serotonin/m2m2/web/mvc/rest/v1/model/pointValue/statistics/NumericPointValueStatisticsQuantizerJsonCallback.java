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
import com.serotonin.m2m2.vo.DataPointVO;
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
	public NumericPointValueStatisticsQuantizerJsonCallback(JsonGenerator jgen, DataPointVO vo, 
			boolean useRendered,  boolean unitConversion, RollupEnum rollup) {
		super(jgen, vo, useRendered, unitConversion);
		this.rollup = rollup;
	}	
	
	
	@Override
    public void quantizedStatistics(AnalogStatistics statisticsGenerator, boolean done) {

		try{
	        if (statisticsGenerator.getCount() > 0 || !done) {
	            switch(rollup){
	                case AVERAGE:
	                	this.writeNonNull(statisticsGenerator.getAverage(), statisticsGenerator.getPeriodStartTime());
	                break;
	                case MINIMUM:
	                	this.writeNonNull(statisticsGenerator.getMinimumValue(), statisticsGenerator.getPeriodStartTime());
	                break;
	                case MAXIMUM:
	                	this.writeNonNull(statisticsGenerator.getMaximumValue(), statisticsGenerator.getPeriodStartTime());
	                break;
                    case ACCUMULATOR:
                        Double accumulatorValue = statisticsGenerator.getLastValue();
                        if (accumulatorValue == null) {
                            accumulatorValue = statisticsGenerator.getMaximumValue();
                        }
                        this.writeNonNull(accumulatorValue, statisticsGenerator.getPeriodStartTime());
	                break;
	                case SUM:
	                	this.writeNonNull(statisticsGenerator.getSum(), statisticsGenerator.getPeriodStartTime());
	                break;
	                case FIRST:
	                	this.writeNonNull(statisticsGenerator.getFirstValue(), statisticsGenerator.getPeriodStartTime());
	                break;
	                case LAST:
	                	this.writeNonNull(statisticsGenerator.getLastValue(), statisticsGenerator.getPeriodStartTime());
	                break;
	                case COUNT:
	                	this.writePointValueTime(statisticsGenerator.getCount(),
	                			statisticsGenerator.getPeriodStartTime(), null);
	                break;
	                case INTEGRAL:
	                	this.writeNonNullIntegral(statisticsGenerator.getIntegral(), statisticsGenerator.getPeriodStartTime());
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
