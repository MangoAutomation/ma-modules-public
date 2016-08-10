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
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.statistics.ParentStatisticsQuantizerCallback;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * @author Terry Packer
 *
 */
public class IdPointValueStatisticsQuantizerJsonCallback implements ParentStatisticsQuantizerCallback{

	private static final Log LOG = LogFactory.getLog(IdPointValueStatisticsQuantizerJsonCallback.class);
			
	protected final String TIMESTAMP = "timestamp";
	protected final String ROLLUP = "rollup";
	
	protected boolean useRendered;
	protected boolean unitConversion;
	protected RollupEnum rollup;
	protected JsonGenerator jgen;
	protected Map<Integer, DataPointVO> voMap;
	
	/**
	 * @param jgen
	 * @param voMap
	 * @param useRendered
	 * @param unitConversion
	 * @param rollup
	 */
	public IdPointValueStatisticsQuantizerJsonCallback(JsonGenerator jgen, Map<Integer, DataPointVO> voMap,
			boolean useRendered, boolean unitConversion, RollupEnum rollup) {
		this.jgen = jgen;
		this.voMap = voMap;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.rollup = rollup;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.statistics.ParentStatisticsQuantizerCallback#closePeriod(java.util.Map, long)
	 */
	@Override
	public void closePeriod(Map<Integer, StatisticsGenerator> periodStatsMap, long periodStartTime) {
		
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
//TODO uncomment for 2.8 when we get ALL Rollup type		            
//		            case ALL:
//		            	this.jgen.writeObjectFieldStart(vo.getXid());
//		            	this.writeDataValue(statisticsGenerator.getFirstValue(), vo, RollupEnum.FIRST.name());
//		            	this.writeDataValue(statisticsGenerator.getLastValue(), vo, RollupEnum.LAST.name());
//		            	this.jgen.writeNumberField(vo.getXid(), statisticsGenerator.getCount());
//		            	this.jgen.writeEndObject();
//		            break;
		            case FIRST:
		            	this.writeDataValue(statisticsGenerator.getFirstValue(), vo, vo.getXid());
		            break;
		            case LAST:
		            	this.writeDataValue(statisticsGenerator.getLastValue(), vo, vo.getXid());
		            break;
		            case COUNT:
		            	this.jgen.writeNumberField(vo.getXid(), statisticsGenerator.getCount());
		            break;
		            default:
		            	//Default to first
		            	this.writeDataValue(statisticsGenerator.getFirstValue(), vo, vo.getXid());
		            }
	
				}else if(stats instanceof AnalogStatistics){
					AnalogStatistics statisticsGenerator = (AnalogStatistics)stats;
		            switch(rollup){
//TODO uncomment for 2.8 when we get ALL Rollup type
//		            case ALL:
//		            	this.jgen.writeObjectFieldStart(vo.getXid());
//		            	this.writeDouble(statisticsGenerator.getAverage(), vo, RollupEnum.AVERAGE.name());
//		            	this.writeDouble(statisticsGenerator.getDelta(), vo, RollupEnum.DELTA.name());
//		            	this.writeDouble(statisticsGenerator.getMinimumValue(), vo, RollupEnum.MINIMUM.name());
//		            	this.writeDouble(statisticsGenerator.getMaximumValue(), vo, RollupEnum.MAXIMUM.name());
//		            	Double acc = statisticsGenerator.getLastValue();
//                        if (acc == null) {
//                            acc = statisticsGenerator.getMaximumValue();
//                        }
//                        this.writeDouble(acc, vo, RollupEnum.ACCUMULATOR.name());
//                        this.writeDouble(statisticsGenerator.getSum(), vo, RollupEnum.SUM.name());
//                        this.writeDouble(statisticsGenerator.getFirstValue(), vo, RollupEnum.FIRST.name());
//                        this.writeDouble(statisticsGenerator.getLastValue(), vo, RollupEnum.LAST.name());
//                        this.writeIntegral(statisticsGenerator.getIntegral(), vo, RollupEnum.INTEGRAL.name());
//		            	this.jgen.writeNumberField(vo.getXid(), statisticsGenerator.getCount());
//		            	this.jgen.writeEndObject();
//		            break;
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

	/**
	 * @param xid
	 * @param firstValue
	 * @throws IOException 
	 */
	private void writeDataValue(DataValue value, DataPointVO vo, String name) throws IOException {
		
		if(value == null){
			this.jgen.writeNullField(name);
		}else{
	    	if(useRendered){
				this.jgen.writeStringField(name, vo.getTextRenderer().getText(value, TextRenderer.HINT_FULL));
			}else if(unitConversion){
				//Convert Value, must be numeric
				if (value instanceof NumericValue)
					this.jgen.writeNumberField(name, vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value.getDoubleValue()));
				else
					this.writeDataValue(name, value);
			}else{
				this.writeDataValue(name, value);
			}
		}
	}
	
	protected void writeDouble(Double value, DataPointVO vo, String name) throws IOException{
		if(value == null){
			this.jgen.writeNullField(name);
		}else{
	    	if(useRendered){
	    		this.jgen.writeStringField(name, vo.getTextRenderer().getText(value, TextRenderer.HINT_FULL));
			}else if(unitConversion){
				this.jgen.writeNumberField(name, vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value));
			}else{
				this.jgen.writeNumberField(name, value);
			}
		}
	}
	
	/**
	 * @param integral
	 * @param vo
	 * @throws IOException 
	 */
	protected void writeIntegral(Double integral, DataPointVO vo, String name) throws IOException {
		if(integral == null){
			this.jgen.writeNullField(name);
		}else{
	    	if(useRendered){
	    		this.jgen.writeStringField(name, Functions.getIntegralText(vo, integral));
			}else{
				this.jgen.writeNumberField(name, integral);
			}
		}
	}
	
	/**
	 * Only to be called via the other write methods
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	private void writeDataValue(String name, DataValue value) throws IOException{
		switch(value.getDataType()){
		case DataTypes.ALPHANUMERIC:
			this.jgen.writeStringField(name, value.getStringValue());
		break;
		case DataTypes.BINARY:
			this.jgen.writeBooleanField(name, value.getBooleanValue());
		break;
		case DataTypes.MULTISTATE:
			this.jgen.writeNumberField(name, value.getIntegerValue());
		break;
		case DataTypes.NUMERIC:
			this.jgen.writeNumberField(name, value.getDoubleValue());
		break;
		case DataTypes.IMAGE:
			this.jgen.writeStringField("value","unsupported-value-type");
			LOG.error("Unsupported data type for Point Value Time: " + value.getDataType());
		break;
		}
	}

}
