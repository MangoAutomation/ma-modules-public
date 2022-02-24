/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;

/**
 * @author Jared Wiltshire
 */
public class DataValueSerializer extends StdScalarSerializer<DataValue> {

    public DataValueSerializer() {
        super(DataValue.class);
    }

    @Override
    public void serialize(DataValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            provider.defaultSerializeNull(gen);
        } else {
            provider.defaultSerializeValue(value.getObjectValue(), gen);
        }
    }
}
