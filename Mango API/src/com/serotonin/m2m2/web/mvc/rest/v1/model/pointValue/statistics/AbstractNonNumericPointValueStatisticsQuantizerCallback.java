/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.view.quantize2.StatisticsGeneratorQuantizerCallback;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.LimitCounter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 * @author Terry Packer
 *
 */
public abstract class AbstractNonNumericPointValueStatisticsQuantizerCallback implements StatisticsGeneratorQuantizerCallback<ValueChangeCounter>{

	private final Log LOG = LogFactory.getLog(AbstractNonNumericPointValueStatisticsQuantizerCallback.class);
	private RollupEnum rollup;
	private PointValueTimeWriter writer;
	private final DataPointVO vo;
	private final LimitCounter limiter;
	

	/**
	 * 
	 * @param writer
	 * @param rollup
	 */
	public AbstractNonNumericPointValueStatisticsQuantizerCallback(DataPointVO vo, PointValueTimeWriter writer, RollupEnum rollup, Integer limit) {
		this.vo = vo;
		this.writer= writer;
		this.rollup = rollup;
		this.limiter = new LimitCounter(limit);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.view.quantize2.StatisticsGeneratorQuantizerCallback#quantizedStatistics(com.serotonin.m2m2.view.stats.StatisticsGenerator, boolean)
	 */
	@Override
    public void quantizedStatistics(ValueChangeCounter statisticsGenerator, boolean done) {
		if(this.limiter.limited())
			return;
		
		try{
	            switch(rollup){
	            case ALL:
	            	this.writer.writeAllStatistics(statisticsGenerator, this.vo);
	            break;
	            case FIRST:
	            	if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
	            		this.writer.writeNonNullImage(statisticsGenerator.getFirstValue(), statisticsGenerator.getFirstTime(), statisticsGenerator.getPeriodStartTime(), this.vo);
	            	else
	            		this.writer.writeNonNull(statisticsGenerator.getFirstValue(), statisticsGenerator.getPeriodStartTime(), this.vo);
	            break;
	            case LAST:
	            	if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
	            		this.writer.writeNonNullImage(statisticsGenerator.getFirstValue(), statisticsGenerator.getLastTime(), statisticsGenerator.getPeriodStartTime(), this.vo);
	            	else
	            		this.writer.writeNonNull(statisticsGenerator.getLastValue(), statisticsGenerator.getPeriodStartTime(), this.vo);
	            break;
	            case COUNT:
	            	this.writer.writePointValueTime(statisticsGenerator.getCount(), statisticsGenerator.getPeriodStartTime(), null, this.vo);
	            break;
	            default:
	            	throw new ShouldNeverHappenException("Unsupported Non-numerical Rollup type: " + rollup);
	       
	            }
		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
    }


}
