/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.taglib.Functions;

/**
 * Create JSON Output for multiple data point's point values of the form:
 * 
 * [
 *  {
 *   "timestamp": 1470696259000,
 *   "xid_one": 23.44,
 *   "xid_two": 11.33,
 *   "xid_three": 57.9
 * },
 * {
 *   "timestamp": 1470696259000,
 *   "xid_one": 23.44,
 *   "xid_three": 57.9
 * },
 * {
 *   "timestamp": 1470696259000,
 *   "xid_two": 11.33,
 *  "xid_three": 57.9
 * }
 *]
 * 
 * 
 * @author Terry Packer
 *
 */
public class IdPointValueTimeJsonStreamCallback extends PointValueTimeJsonWriter implements MappedRowCallback<IdPointValueTime>{

	private final Log LOG = LogFactory.getLog(IdPointValueTimeJsonStreamCallback.class);
	private final String TIMESTAMP = "timestamp";
	
	private final Map<Integer, DataPointVO> voMap;
	
	private final Map<String, PointDataValue> currentValueMap;
	private long currentTime;
	private boolean objectOpen;
	
	/**
	 * @param jgen
	 */
	public IdPointValueTimeJsonStreamCallback(String host, int port, JsonGenerator jgen, Map<Integer,DataPointVO> voMap, boolean useRendered,  boolean unitConversion) {
		super(host, port, jgen, useRendered, unitConversion);
		this.voMap = voMap;
		this.currentValueMap = new HashMap<String, PointDataValue>(voMap.size());
		this.currentTime = Long.MIN_VALUE;
		this.objectOpen = false;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(IdPointValueTime pvt, int index) {
		try{
			DataPointVO vo = this.voMap.get(pvt.getDataPointId());
			long time = pvt.getTime();
			
			//Ensure we are writing into the appropraite entry
			if(this.currentTime != time){
				//Flush an entry if we have one
				if(objectOpen){
					writeEntry();
				}
				this.objectOpen = true;
				this.currentTime = time;
			}
			
			if(useRendered){
				//Convert to Alphanumeric Value
				this.currentValueMap.put(vo.getXid(), new PointDataValue(vo, new AlphanumericValue(Functions.getRenderedText(vo, pvt))));
			}else if(unitConversion){
				if (pvt.getValue() instanceof NumericValue)
					this.currentValueMap.put(vo.getXid(), new PointDataValue(vo, new NumericValue(vo.getUnit().getConverterTo(vo.getRenderedUnit()).convert(pvt.getValue().getDoubleValue()))));
				else
					this.currentValueMap.put(vo.getXid(), new PointDataValue(vo, pvt.getValue()));
			}else{
				if(vo.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
					this.currentValueMap.put(vo.getXid(), new PointDataValue(vo, new AlphanumericValue(imageServletBuilder.buildAndExpand(pvt.getTime(), vo.getId()).toUri().toString())));
				else
					this.currentValueMap.put(vo.getXid(), new PointDataValue(vo, pvt.getValue()));
			}
		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
		
	}
	


	/**
	 * @param xid2
	 * @param value
	 * @throws IOException 
	 */
	private void writeXidPointValue(String xid, long timestamp, DataValue value, DataPointVO vo) throws IOException {
		if(value == null){
			this.jgen.writeNullField(xid);
		}else{
			switch(value.getDataType()){
				case DataTypes.ALPHANUMERIC:
					this.jgen.writeStringField(xid, value.getStringValue());
				break;
				case DataTypes.BINARY:
					this.jgen.writeBooleanField(xid, value.getBooleanValue());
				break;
				case DataTypes.MULTISTATE:
					this.jgen.writeNumberField(xid, value.getIntegerValue());
				break;
				case DataTypes.NUMERIC:
					this.jgen.writeNumberField(xid, value.getDoubleValue());
				break;
				case DataTypes.IMAGE:
					this.jgen.writeStringField(xid,imageServletBuilder.buildAndExpand(timestamp, vo.getId()).toUri().toString());
				break;
				default:
					LOG.error("Unsupported data type for Point Value Time: " + value.getDataType());
				break;
			}
		}
	}

	private void writeEntry() throws IOException{
		this.jgen.writeStartObject();
		this.jgen.writeNumberField(TIMESTAMP, this.currentTime);
		Iterator<String> it = this.currentValueMap.keySet().iterator();
		String xid;
		while(it.hasNext()){
			xid = it.next();
			PointDataValue value = this.currentValueMap.get(xid);
			if(value != null){
				this.writeXidPointValue(xid, this.currentTime, value.value, value.vo);
			}
		}
		jgen.writeEndObject();
		this.currentValueMap.clear();
	}
	/**
	 * Finish any open objects
	 * @throws IOException
	 */
	public void finish(){
		if(this.objectOpen){
			try {
				this.writeEntry();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	class PointDataValue{
		DataPointVO vo;
		DataValue value;
		
		public PointDataValue(DataPointVO vo, DataValue value){
			this.vo = vo;
			this.value = value;
		}
		
	}
	
}
