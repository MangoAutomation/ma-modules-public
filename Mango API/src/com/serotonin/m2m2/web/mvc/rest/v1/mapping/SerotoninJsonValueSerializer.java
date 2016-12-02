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
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonWriter;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;

/**
 * Serialize Mango Objects Using Serotonin JSON
 * 
 * @author Terry Packer
 */
public class SerotoninJsonValueSerializer extends JsonSerializer<JsonValue>{
	
	@Override
	public void serialize(JsonValue value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		JsonWriter writer = new JsonWriter(Common.JSON_CONTEXT, new SerotoninToJacksonJsonWriter(jgen));
		writer.setPrettyIndent(3);//TODO Make configurable
		writer.setPrettyOutput(true);
		try{
			writer.writeObject(value);
		}
		catch (JsonException e) {
            throw new IOException(e);
        }
	}
}
