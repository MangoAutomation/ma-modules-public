/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.serotonin.m2m2.web.mvc.rest.v1.model.JsonStream;

/**
 * Stream any object out using a JsonGenerator
 * 
 * @author Jared Wiltshire
 */
public class JsonStreamSerializer<T> extends JsonSerializer<JsonStream<T>> {

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(JsonStream<T> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        value.streamData(jgen);
    }

}
