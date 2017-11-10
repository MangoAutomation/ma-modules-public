/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.taglib.Functions;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * 
 * CSV Format is point per column, all points plus time columns per row.  Useful in multi point 
 * queries
 * 
 * @author Terry Packer
 *
 */
public class IdPointValueTimeCsvStreamCallback extends PointValueTimeCsvWriter implements MappedRowCallback<IdPointValueTime>{

	private final Log LOG = LogFactory.getLog(PointValueTimeJsonStreamCallback.class);
	private final String TIMESTAMP = "timestamp";
	
	private final LimitCounter limiter;
	private final Map<Integer, DataPointVO> voMap;
	private long currentTime;
	
	//Map of data point Id to column ID
	private final Map<Integer, Integer> columnMap;
	private final String[] headers;
	private String[] rowData;
	private boolean wroteHeaders;
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param writer
	 * @param voMap
	 * @param useRendered
	 * @param unitConversion
	 * @param limit
	 * @param dateTimeFormat - format for date strings, if null then use epoch millis number
	 * @param timezone
	 */
	public IdPointValueTimeCsvStreamCallback(String host, int port, CSVWriter writer, Map<Integer, DataPointVO> voMap, boolean useRendered,  boolean unitConversion, Integer limit, String dateTimeFormat, String timezone) {
		super(host, port, writer, useRendered, unitConversion, dateTimeFormat, timezone);
		this.limiter = new LimitCounter(limit);
		this.voMap = voMap;
		this.currentTime = Long.MIN_VALUE;
		
		this.wroteHeaders = false;
		this.columnMap = new HashMap<Integer,Integer>(voMap.size());
		this.rowData = new String[voMap.size() + 1];
		this.headers = new String[voMap.size() + 1];
		this.headers[0] = TIMESTAMP;
		
		//Generate the column Ids and Header
		int columnId = 1;
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
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(IdPointValueTime pvt, int index) {
		
		try{
			DataPointVO vo = this.voMap.get(pvt.getId());
			long time = pvt.getTime();
			if(dateFormatter == null)
			    this.rowData[0] = Long.toString(time);
			else
			    this.rowData[0] = dateFormatter.format(ZonedDateTime
	                    .ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId()));
			
			//Ensure we are saving into the correct time entry
			if(this.currentTime != time){

				if(!wroteHeaders){
					this.writer.writeNext(headers);
					this.wroteHeaders = true;
				}else{
					//Write the line
					if(this.limiter.limited())
						return;
					this.writer.writeNext(rowData);
					for(int i=0; i< this.rowData.length; i++)
						this.rowData[i] = new String();
				}
				
				this.currentTime = time;
			}
			
			if(useRendered){
				//Convert to Alphanumeric Value
				this.rowData[this.columnMap.get(vo.getId())] = Functions.getRenderedText(vo, pvt);
			}else if(unitConversion){
				if (pvt.getValue() instanceof NumericValue)
					this.rowData[this.columnMap.get(vo.getId())] = Double.toString(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(pvt.getValue().getDoubleValue()));
				else
					this.rowData[this.columnMap.get(vo.getId())] = this.createDataValueString(pvt.getValue());
			}else{
				if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
					this.rowData[this.columnMap.get(vo.getId())] = imageServletBuilder.buildAndExpand(pvt.getTime(), vo.getId()).toUri().toString();
				else
					this.rowData[this.columnMap.get(vo.getId())] = this.createDataValueString(pvt.getValue());
			}
		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
	}
	
	private String createDataValueString(DataValue value){
		if(value == null){
			return "";
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
				default:
					LOG.error("Unsupported data type for DataValue: " + value.getDataType());
					return "unsupported-value-type";
			}
		}
	}

	/**
	 * Finish any open objects
	 * @throws IOException
	 */
	public void finish(){
		if((this.wroteHeaders)&&(!this.limiter.limited())){
			this.writer.writeNext(rowData);
		}
	}
}
