/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;

/**
 * @author Terry Packer
 *
 */
public class ObjectJsonStreamCallback<T> implements MappedRowCallback<T>{

	private final Log LOG = LogFactory.getLog(ObjectJsonStreamCallback.class);
	
	protected JsonGenerator jgen;
	
	public void setJsonGenerator(JsonGenerator jgen){
		this.jgen = jgen;
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(T vo, int index) {
		try {
			this.write(vo);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
	}
	
	/**
	 * Do the work of writing the VO
	 * @param vo
	 * @throws IOException
	 */
	protected void write(T vo) throws IOException{
		this.jgen.writeObject(vo);
	}

}
