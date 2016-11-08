/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.statistics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.view.stats.StatisticsGenerator;
import com.serotonin.m2m2.view.stats.ValueChangeCounter;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.PointValueTimeCsvWriter;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;
import com.serotonin.m2m2.web.mvc.rest.v1.statistics.ParentStatisticsQuantizerCallback;
import com.serotonin.m2m2.web.taglib.Functions;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Terry Packer
 *
 */
public class IdPointValueStatisticsQuantizerCsvCallback extends PointValueTimeCsvWriter implements ParentStatisticsQuantizerCallback{

	private final Log LOG = LogFactory.getLog(IdPointValueStatisticsQuantizerCsvCallback.class);

	private final String TIMESTAMP = "timestamp";
	private final String ROLLUP = "rollup";
	
	protected boolean useRendered;
	protected boolean unitConversion;
	protected RollupEnum rollup;
	protected CSVWriter writer;
	protected Map<Integer, DataPointVO> voMap;
	protected Map<Integer, Integer> columnMap;
	protected final String[] headers;
	protected final String[] rowData;
	protected boolean wroteHeaders;
	
	/**
	 * @param writer
	 * @param voMap
	 * @param useRendered
	 * @param unitConversion
	 * @param rollup
	 */
	public IdPointValueStatisticsQuantizerCsvCallback(String host, int port, CSVWriter writer, Map<Integer, DataPointVO> voMap,
			boolean useRendered, boolean unitConversion, RollupEnum rollup) {
		super(host, port, writer, useRendered, unitConversion);
		this.writer = writer;
		this.voMap = voMap;
		this.useRendered = useRendered;
		this.unitConversion = unitConversion;
		this.rollup = rollup;
		
		this.wroteHeaders = false;
		this.columnMap = new HashMap<Integer,Integer>(voMap.size());
		this.rowData = new String[voMap.size() + 2];
		this.rowData[0] = this.rollup.name();
		this.headers = new String[voMap.size() + 2];
		this.headers[0] = ROLLUP;
		this.headers[1] = TIMESTAMP;
		
		//Generate the column Ids and Header
		int columnId = 2;
		DataPointVO vo;
		Iterator<Integer> it = this.voMap.keySet().iterator();
		while(it.hasNext()){
			Integer id = it.next();
			vo = voMap.get(id);
			this.columnMap.put(id, columnId);
			this.headers[columnId] = vo.getXid();
			columnId++;
		}
		
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.web.mvc.rest.v1.statistics.ParentStatisticsQuantizerCallback#closePeriod(java.util.Map, long)
	 */
	@Override
	public void closePeriod(Map<Integer, StatisticsGenerator> periodStatsMap, long periodStartTime) {
		
		try{
			if(!wroteHeaders){
				this.writer.writeNext(headers);
				this.wroteHeaders = true;
			}
			
			//Write out the period start time
			this.rowData[1] = Long.toString(periodStartTime);
			Iterator<Integer> it = periodStatsMap.keySet().iterator();
			while(it.hasNext()){
				Integer id = it.next();
				StatisticsGenerator stats = periodStatsMap.get(id);
				DataPointVO vo = this.voMap.get(id);
				if(stats instanceof ValueChangeCounter){
					ValueChangeCounter statisticsGenerator = (ValueChangeCounter)stats;
		            switch(rollup){
		            case FIRST:
		            	if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
		            		this.writeDataValue(statisticsGenerator.getFirstTime(), statisticsGenerator.getFirstValue(), vo);
		            	else
		            		this.writeDataValue(periodStartTime, statisticsGenerator.getFirstValue(), vo);
		            break;
		            case LAST:
		            	if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
		            		this.writeDataValue(statisticsGenerator.getLastTime(), statisticsGenerator.getLastValue(), vo);
		            	else
		            		this.writeDataValue(periodStartTime, statisticsGenerator.getLastValue(), vo);
		            break;
		            case COUNT:
		            	this.rowData[this.columnMap.get(vo.getId())] = Integer.toString(statisticsGenerator.getCount());
		            break;
		            default:
		            	//Default to first
		            	this.writeDataValue(periodStartTime, statisticsGenerator.getFirstValue(), vo);
		            }
	
				}else if(stats instanceof AnalogStatistics){
					AnalogStatistics statisticsGenerator = (AnalogStatistics)stats;
		            switch(rollup){
	                case AVERAGE:
	                	this.writeDouble(statisticsGenerator.getAverage(), vo);
	                break;
	                case DELTA:
	                	this.writeDouble(statisticsGenerator.getDelta(), vo);
	                break;
	                case MINIMUM:
	                	this.writeDouble(statisticsGenerator.getMinimumValue(), vo);
	                break;
	                case MAXIMUM:
	                	this.writeDouble(statisticsGenerator.getMaximumValue(), vo);
	                break;
	                //TODO This should be removed after discussion with Jared
	                case ACCUMULATOR:
	                    Double accumulatorValue = statisticsGenerator.getLastValue();
	                    if (accumulatorValue == null) {
	                        accumulatorValue = statisticsGenerator.getMaximumValue();
	                    }
	                    this.writeDouble(accumulatorValue, vo);
	                break;
	                case SUM:
	                	this.writeDouble(statisticsGenerator.getSum(), vo);
	                break;
	                case FIRST:
	                	this.writeDouble(statisticsGenerator.getFirstValue(), vo);
	                break;
	                case LAST:
	                	this.writeDouble(statisticsGenerator.getLastValue(), vo);
	                break;
	                case COUNT:
	                	this.rowData[this.columnMap.get(vo.getId())] = Integer.toString(statisticsGenerator.getCount());
	                break;
	                case INTEGRAL:
	                	this.writeIntegral(statisticsGenerator.getIntegral(), vo);
	                break;
	                default:
	                	//Default to first
	                	this.writeDouble(statisticsGenerator.getFirstValue(), vo);
		            }
				}else{
					throw new ShouldNeverHappenException("Unsuported statistics type: " + stats.getClass().getName());
				}
			}
			this.writer.writeNext(this.rowData);
			//Clear out the row data
			for(int i=1; i<this.rowData.length; i++)
				this.rowData[i] = null;

		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * @param xid
	 * @param firstValue
	 * @throws IOException 
	 */
	private void writeDataValue(long timestamp, DataValue value, DataPointVO vo) throws IOException {
		
		if(value == null){
			this.rowData[this.columnMap.get(vo.getId())] = null;
		}else{
	    	if(useRendered){
	    		this.rowData[this.columnMap.get(vo.getId())] = vo.getTextRenderer().getText(value, TextRenderer.HINT_FULL);
			}else if(unitConversion){
				//Convert Value, must be numeric
				if (value instanceof NumericValue)
					this.rowData[this.columnMap.get(vo.getId())] = Double.toString(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value.getDoubleValue()));
				else
					this.rowData[this.columnMap.get(vo.getId())] = this.createDataValueString(value, timestamp, vo);
			}else{
				this.rowData[this.columnMap.get(vo.getId())] = this.createDataValueString(value, timestamp, vo);
			}
		}
	}
	
	protected void writeDouble(Double value, DataPointVO vo) throws IOException{
		if(value == null){
			this.rowData[this.columnMap.get(vo.getId())] = null;
		}else{
	    	if(useRendered){
	    		this.rowData[this.columnMap.get(vo.getId())] = vo.getTextRenderer().getText(value, TextRenderer.HINT_FULL);
			}else if(unitConversion){
				this.rowData[this.columnMap.get(vo.getId())] = Double.toString(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(value));
			}else{
				this.rowData[this.columnMap.get(vo.getId())] = Double.toString(value);
			}
		}
	}
	
	/**
	 * @param integral
	 * @param vo
	 * @throws IOException 
	 */
	protected void writeIntegral(Double integral, DataPointVO vo) throws IOException {
		if(integral == null){
			this.rowData[this.columnMap.get(vo.getId())] = null;
		}else{
	    	if(useRendered){
	    		this.rowData[this.columnMap.get(vo.getId())] = Functions.getIntegralText(vo, integral);
			}else{
				this.rowData[this.columnMap.get(vo.getId())] = Double.toString(integral);
			}
		}
	}
	
	/**
	 * Only to be called via the other write methods
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	private String createDataValueString(DataValue value, long timestamp, DataPointVO vo){
		if(value == null){
			return null;
		}else{
			switch(value.getDataType()){
				case DataTypes.ALPHANUMERIC:
					return value.getStringValue();
				case DataTypes.BINARY:
					return Boolean.toString(value.getBooleanValue());
				case DataTypes.MULTISTATE:
					return Integer.toString(value.getIntegerValue());
				case DataTypes.NUMERIC:
					return Double.toString(value.getDoubleValue());
				case DataTypes.IMAGE:
					return imageServletBuilder.buildAndExpand(timestamp, vo.getId()).toUri().toString();
				default:
					return "unsupported-value-type";
			}
		}
	}
	
}
