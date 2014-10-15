/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonObjectStream;

/**
 * Wrap any object in JSON { } 
 * 
 * 
 * @author Terry Packer
 *
 */
public class JsonObjectSerializer extends JsonSerializer<JsonObjectStream>{

	/* (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(JsonObjectStream value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {

		jgen.writeStartObject();
		value.streamData(jgen);
		jgen.writeEndObject();
		
	}

}
