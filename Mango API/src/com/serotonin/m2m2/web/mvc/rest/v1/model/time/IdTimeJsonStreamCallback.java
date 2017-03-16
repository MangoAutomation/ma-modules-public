/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.time;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.rt.dataImage.IdTime;
import com.serotonin.m2m2.vo.DataPointVO;

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

	
	public IdTimeJsonStreamCallback(XidTimeJsonWriter<T> writer, JsonGenerator jgen, Map<Integer, DataPointVO> voMap){
		this.writer = writer;
		this.jgen = jgen;
		this.voMap = voMap;
		this.currentValueMap = new HashMap<DataPointVO, T>(voMap.size());
		this.currentTime = Long.MIN_VALUE;
		this.objectOpen = false;

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
				if(objectOpen){
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
		this.jgen.writeNumberField(TIMESTAMP, this.currentTime);
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
		if(this.objectOpen){
			try {
				this.writeEntry();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
}
