/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.events;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.vo.event.EventInstanceVO;

/**
 * @author Terry Packer
 *
 */
public class EventInstanceJsonStreamCallback extends EventInstanceJsonWriter implements MappedRowCallback<EventInstanceVO>{

	/**
	 * @param jgen
	 */
	public EventInstanceJsonStreamCallback(JsonGenerator jgen) {
		super(jgen);
	}


	private final Log LOG = LogFactory.getLog(EventInstanceJsonStreamCallback.class);

	
	/* (non-Javadoc)
	 * @see com.serotonin.db.MappedRowCallback#row(java.lang.Object, int)
	 */
	@Override
	public void row(EventInstanceVO vo, int index) {
		try {
			this.writeEvent(vo);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
	}

}
