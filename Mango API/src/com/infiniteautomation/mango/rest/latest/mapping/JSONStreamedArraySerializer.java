/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.mapping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.infiniteautomation.mango.rest.latest.model.JSONStreamedArray;

/**
 *
 * @author Terry Packer
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
