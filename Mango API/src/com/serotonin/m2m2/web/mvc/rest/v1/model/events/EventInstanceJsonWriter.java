/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.model.events;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.vo.event.EventInstanceVO;

/**
 * @author Terry Packer
 *
 */
public class EventInstanceJsonWriter {

	protected JsonGenerator jgen;

	public EventInstanceJsonWriter(JsonGenerator jgen){
		this.jgen = jgen;
	}
	
	protected void writeEvent(EventInstanceVO vo) throws IOException{
		EventModel model = new EventModel(vo);
		jgen.writeObject(model);
	}
}
