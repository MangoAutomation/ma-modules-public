/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.view.quantize2.StatisticsGeneratorQuantizerCallback;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 * @author Terry Packer
 *
 */
public abstract class AbstractNumericPointValueStatisticsQuantizerCallback implements StatisticsGeneratorQuantizerCallback<AnalogStatistics>{

	private final Log LOG = LogFactory.getLog(AbstractNumericPointValueStatisticsQuantizerCallback.class);
	
	private RollupEnum rollup;
	private PointValueTimeWriter writer;
	private final DataPointVO vo;

	/**
	 * 
	 * @param writer
	 * @param rollup
	 */
	public AbstractNumericPointValueStatisticsQuantizerCallback(DataPointVO vo, PointValueTimeWriter writer, RollupEnum rollup) {
		this.vo = vo;
		this.writer = writer;
		this.rollup = rollup;
	}	
	
	
	@Override
    public void quantizedStatistics(AnalogStatistics statisticsGenerator, boolean done) {

		try{
//	        if (statisticsGenerator.getCount() > 0 || !done) {
	            switch(rollup){
	                case AVERAGE:
	                	this.writer.writeNonNullDouble(statisticsGenerator.getAverage(), statisticsGenerator.getPeriodStartTime(), this.vo);
	                break;
	                case DELTA:
	                	this.writer.writeNonNullDouble(statisticsGenerator.getDelta(), statisticsGenerator.getPeriodStartTime(), this.vo);
	                break;
	                case MINIMUM:
	                	this.writer.writeNonNullDouble(statisticsGenerator.getMinimumValue(), statisticsGenerator.getPeriodStartTime(), this.vo);
	                break;
	                case MAXIMUM:
	                	this.writer.writeNonNullDouble(statisticsGenerator.getMaximumValue(), statisticsGenerator.getPeriodStartTime(), this.vo);
	                break;
	                //TODO This should be removed after discussion with Jared
                    case ACCUMULATOR:
                        Double accumulatorValue = statisticsGenerator.getLastValue();
                        if (accumulatorValue == null) {
                            accumulatorValue = statisticsGenerator.getMaximumValue();
                        }
                        this.writer.writeNonNullDouble(accumulatorValue, statisticsGenerator.getPeriodStartTime(), this.vo);
	                break;
	                case SUM:
	                	this.writer.writeNonNullDouble(statisticsGenerator.getSum(), statisticsGenerator.getPeriodStartTime(), this.vo);
	                break;
	                case FIRST:
	                	this.writer.writeNonNullDouble(statisticsGenerator.getFirstValue(), statisticsGenerator.getPeriodStartTime(), this.vo);
	                break;
	                case LAST:
	                	this.writer.writeNonNullDouble(statisticsGenerator.getLastValue(), statisticsGenerator.getPeriodStartTime(), this.vo);
	                break;
	                case COUNT:
	                	this.writer.writePointValueTime(statisticsGenerator.getCount(),
	                			statisticsGenerator.getPeriodStartTime(), null);
	                break;
	                case INTEGRAL:
	                	this.writer.writeNonNullIntegral(statisticsGenerator.getIntegral(), statisticsGenerator.getPeriodStartTime(), this.vo);
                    break;
	                default:
	                	throw new ShouldNeverHappenException("Unknown Rollup type" + rollup);
	            }
//	        }
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
    }
	
}
