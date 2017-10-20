/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.serotonin.m2m2.web.mvc.rest.v1.mapping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.infiniteautomation.mango.rest.v2.model.JSONStreamedArray;

/**
 * @author Jared Wiltshire
 */
public class JSONStreamedArraySerializer extends JsonSerializer<JSONStreamedArray> {

    @Override
    public void serialize(JSONStreamedArray value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {

        gen.writeStartArray();
        value.writeArrayValues(gen);
        gen.writeEndArray();
    }
}
