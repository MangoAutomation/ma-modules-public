/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.mapping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;

/**
 * @author Terry Packer
 *
 */
public class JsonDataPageSerializer<T> extends JsonSerializer<QueryDataPageStream<T>>{

	/* (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	public void serialize(QueryDataPageStream<T> value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		//Write start Result Set
		//jgen.writeStartArray();
		jgen.writeStartObject();

		//Write results start Array Block
		jgen.writeFieldName("items");
		jgen.writeStartArray();
		value.streamData(jgen);
		jgen.writeEndArray();
		
		//Write count via callback (After the main query in case we just wanted to count the values as they come back)
		jgen.writeFieldName("total");
		value.streamCount(jgen);		
		jgen.writeEndObject();
		
	}

}
