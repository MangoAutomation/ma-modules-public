/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.LimitCounter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeJsonWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.statistics.ParentStatisticsQuantizerCallback;

/**
 * @author Terry Packer
 *
 */
public class IdPointValueStatisticsQuantizerJsonCallback extends PointValueTimeJsonWriter implements ParentStatisticsQuantizerCallback {

	private static final Log LOG = LogFactory.getLog(IdPointValueStatisticsQuantizerJsonCallback.class);
			

	
	protected final RollupEnum rollup;
	protected final Map<Integer, DataPointVO> voMap;
	protected final LimitCounter limiter;
	
	/**
	 * @param jgen
	 * @param voMap
	 * @param useRendered
	 * @param unitConversion
	 * @param rollup
	 */
	public IdPointValueStatisticsQuantizerJsonCallback(String host, int port, JsonGenerator jgen, Map<Integer, DataPointVO> voMap,
			boolean useRendered, boolean unitConversion, RollupEnum rollup, Integer limit) {
		super(host, port, jgen, useRendered, unitConversion);
		this.voMap = voMap;
		this.rollup = rollup;
		this.limiter = new LimitCounter(limit);
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.statistics.ParentStatisticsQuantizerCallback#closePeriod(java.util.Map, long)
	 */
	@Override
	public void closePeriod(Map<Integer, StatisticsGenerator> periodStatsMap, long periodStartTime) {
		if(limiter.limited())
			return;
		
		try{
			//Write out the period start time
			this.jgen.writeStartObject();
			this.jgen.writeNumberField(TIMESTAMP, periodStartTime);
			this.jgen.writeStringField(ROLLUP, this.rollup.name());
			
			Iterator<Integer> it = periodStatsMap.keySet().iterator();
			while(it.hasNext()){
				Integer id = it.next();
				StatisticsGenerator stats = periodStatsMap.get(id);
				DataPointVO vo = this.voMap.get(id);
				if(stats instanceof ValueChangeCounter){
					ValueChangeCounter statisticsGenerator = (ValueChangeCounter)stats;
	
		            switch(rollup){	            
		            case ALL:
		            	this.jgen.writeObjectFieldStart(vo.getXid());
		            	if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
		            		this.writeImageValue(statisticsGenerator.getFirstValue(), statisticsGenerator.getFirstTime(), periodStartTime, vo, RollupEnum.FIRST.name());
		            	else
		            		this.writeDataValue(periodStartTime, statisticsGenerator.getFirstValue(), vo, RollupEnum.FIRST.name());
		            	
		            	if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
		            		this.writeImageValue(statisticsGenerator.getLastValue(), statisticsGenerator.getLastTime(), periodStartTime, vo, RollupEnum.LAST.name());
		            	else
		            		this.writeDataValue(periodStartTime, statisticsGenerator.getLastValue(), vo, RollupEnum.LAST.name());
		            	this.jgen.writeNumberField(RollupEnum.COUNT.name(), statisticsGenerator.getCount());
		            	this.jgen.writeEndObject();
		            break;
		            case START:
                        if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
                            this.writeImageValue(statisticsGenerator.getStartValue(), periodStartTime, periodStartTime, vo, vo.getXid());
                        else
                            this.writeDataValue(periodStartTime, statisticsGenerator.getStartValue(), vo, vo.getXid());
                    break;
		            case FIRST:
		            	if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
		            		this.writeImageValue(statisticsGenerator.getFirstValue(), statisticsGenerator.getFirstTime(), periodStartTime, vo, vo.getXid());
		            	else
		            		this.writeDataValue(periodStartTime, statisticsGenerator.getFirstValue(), vo, vo.getXid());
		            break;
		            case LAST:
		            	if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
		            		this.writeImageValue(statisticsGenerator.getLastValue(), statisticsGenerator.getLastTime(), periodStartTime, vo, vo.getXid());
		            	else
		            		this.writeDataValue(periodStartTime, statisticsGenerator.getLastValue(), vo, vo.getXid());
		            break;
		            case COUNT:
		            	this.jgen.writeNumberField(vo.getXid(), statisticsGenerator.getCount());
		            break;
		            default:
		            	//Default to first
		            	this.writeDataValue(periodStartTime, statisticsGenerator.getFirstValue(), vo, vo.getXid());
		            }
	
				}else if(stats instanceof AnalogStatistics){
					AnalogStatistics statisticsGenerator = (AnalogStatistics)stats;
		            switch(rollup){
		            case ALL:
		            	this.jgen.writeObjectFieldStart(vo.getXid());
		            	this.writeDouble(statisticsGenerator.getAverage(), vo, RollupEnum.AVERAGE.name());
		            	this.writeDouble(statisticsGenerator.getDelta(), vo, RollupEnum.DELTA.name());
		            	this.writeDouble(statisticsGenerator.getMinimumValue(), vo, RollupEnum.MINIMUM.name());
		            	this.writeDouble(statisticsGenerator.getMaximumValue(), vo, RollupEnum.MAXIMUM.name());
		            	Double acc = statisticsGenerator.getLastValue();
                        if (acc == null) {
                            acc = statisticsGenerator.getMaximumValue();
                        }
                        this.writeDouble(acc, vo, RollupEnum.ACCUMULATOR.name());
                        this.writeDouble(statisticsGenerator.getSum(), vo, RollupEnum.SUM.name());
                        this.writeDouble(statisticsGenerator.getFirstValue(), vo, RollupEnum.FIRST.name());
                        this.writeDouble(statisticsGenerator.getLastValue(), vo, RollupEnum.LAST.name());
                        this.writeIntegral(statisticsGenerator.getIntegral(), vo, RollupEnum.INTEGRAL.name());
		            	this.jgen.writeNumberField(RollupEnum.COUNT.name(), statisticsGenerator.getCount());
		            	this.jgen.writeEndObject();
		            break;
	                case AVERAGE:
	                	this.writeDouble(statisticsGenerator.getAverage(), vo, vo.getXid());
	                break;
	                case DELTA:
	                	this.writeDouble(statisticsGenerator.getDelta(), vo, vo.getXid());
	                break;
	                case MINIMUM:
	                	this.writeDouble(statisticsGenerator.getMinimumValue(), vo, vo.getXid());
	                break;
	                case MAXIMUM:
	                	this.writeDouble(statisticsGenerator.getMaximumValue(), vo, vo.getXid());
	                break;
	                //TODO This should be removed after discussion with Jared
                    case ACCUMULATOR:
                        Double accumulatorValue = statisticsGenerator.getLastValue();
                        if (accumulatorValue == null) {
                            accumulatorValue = statisticsGenerator.getMaximumValue();
                        }
                        this.writeDouble(accumulatorValue, vo, vo.getXid());
	                break;
	                case SUM:
	                	this.writeDouble(statisticsGenerator.getSum(), vo, vo.getXid());
	                break;
	                case START:
                        this.writeDouble(statisticsGenerator.getStartValue(), vo, vo.getXid());
                    break;
	                case FIRST:
	                	this.writeDouble(statisticsGenerator.getFirstValue(), vo, vo.getXid());
	                break;
	                case LAST:
	                	this.writeDouble(statisticsGenerator.getLastValue(), vo, vo.getXid());
	                break;
	                case COUNT:
	                	this.jgen.writeNumberField(vo.getXid(), statisticsGenerator.getCount());
	                break;
	                case INTEGRAL:
	                	this.writeIntegral(statisticsGenerator.getIntegral(), vo, vo.getXid());
                    break;
	                default:
	                	//Default to first
	                	this.writeDouble(statisticsGenerator.getFirstValue(), vo, vo.getXid());
		            }
				}else{
					throw new ShouldNeverHappenException("Unsuported statistics type: " + stats.getClass().getName());
				}
			}
			this.jgen.writeEndObject();
		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
	}
}
