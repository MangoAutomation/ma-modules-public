/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.time;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.rt.dataImage.IdTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.pointValue.LimitCounter;

/**
 * Abstract Base Class to Write IdTime Objects
 * out to a stream in the form:
 * 
 * [
 *  { 'timestamp': ts,
 *    'xid1': value,
 *    'xid2': value,
 *  }, ...
 * ]
 *   
 * 
 * 
 * @author Terry Packer
 */
public abstract class IdTimeJsonStreamCallback<T extends IdTime> implements MappedRowCallback<T>{

	protected final Log LOG = LogFactory.getLog(IdTimeJsonStreamCallback.class);
	protected final String TIMESTAMP = "timestamp";
	
	protected XidTimeJsonWriter<T> writer;
	protected JsonGenerator jgen;
	protected final Map<Integer, DataPointVO> voMap;
	protected final Map<DataPointVO, T> currentValueMap;
	protected long currentTime;
	protected boolean objectOpen;
	protected final LimitCounter limiter;
	protected final DateTimeFormatter dateFormatter;
	protected final ZoneId zoneId;
	
	/**
	 * 
	 * @param writer
	 * @param jgen
	 * @param voMap
	 * @param limit
	 * @param dateTimeFormat - format for String dates or null for timestamp numbers
	 * @param timezone
	 */
	public IdTimeJsonStreamCallback(XidTimeJsonWriter<T> writer, JsonGenerator jgen, Map<Integer, DataPointVO> voMap, Integer limit, String dateTimeFormat, String timezone){
		this.writer = writer;
		this.jgen = jgen;
		this.voMap = voMap;
		this.currentValueMap = new HashMap<DataPointVO, T>(voMap.size());
		this.currentTime = Long.MIN_VALUE;
		this.objectOpen = false;
		this.limiter = new LimitCounter(limit);

		if(dateTimeFormat != null) {
            this.dateFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
            if(timezone == null)
                this.zoneId = TimeZone.getDefault().toZoneId();
            else
                this.zoneId = ZoneId.of(timezone);
        }else {
            this.dateFormatter = null;
            this.zoneId = null;
        }
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(T item, int index) {
		
		try{
			DataPointVO vo = this.voMap.get(item.getId());
			long time = item.getTime();
			
			//Ensure we are writing into the appropriate entry
			if(this.currentTime != time){
				//Flush an entry if we have one
				if(this.objectOpen){
					if(this.limiter.limited())
						return;
					writeEntry();
				}
				this.objectOpen = true;
				this.currentTime = time;
			}
			
			this.currentValueMap.put(vo, item);
		}catch(IOException e){
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Write one element of the output array
	 * 
	 * @throws IOException
	 */
	protected void writeEntry() throws IOException{
		this.jgen.writeStartObject();
		
		if (dateFormatter == null)
            jgen.writeNumberField(TIMESTAMP, this.currentTime);
        else
            jgen.writeStringField(TIMESTAMP, dateFormatter.format(ZonedDateTime
                    .ofInstant(Instant.ofEpochMilli(this.currentTime), zoneId)));
		
		Iterator<DataPointVO> it = this.currentValueMap.keySet().iterator();
		DataPointVO vo;
		while(it.hasNext()){
			vo = it.next();
			T value = this.currentValueMap.get(vo);
			if(value != null){
				this.writer.writeXidTime(this.jgen, vo, value);
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
		if((this.objectOpen)&&(!this.limiter.limited())){
			try {
				this.writeEntry();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
}
