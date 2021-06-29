/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.genericcsv;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * @author Jared Wiltshire
 */
public class LongAsDateSerializer extends StdScalarSerializer<Long> implements ContextualSerializer {

    private static final long serialVersionUID = 1L;

    private final JsonSerializer<Object> dateSerializer;
    private final Long nullValue;

    public LongAsDateSerializer() {
        super(Long.class);
        this.dateSerializer = null;
        this.nullValue = null;
    }

    protected LongAsDateSerializer(JsonSerializer<Object> dateSerializer, Long nullValue) {
        super(Long.class);
        this.dateSerializer = dateSerializer;
        this.nullValue = nullValue;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        LongAsDate annotation;
        if (property != null && (annotation = property.getAnnotation(LongAsDate.class)) != null) {
            Long nullValue = null;
            if (annotation.useNullValue()) {
                nullValue = annotation.nullValue();
            }

            return new LongAsDateSerializer(prov.findValueSerializer(Date.class, property), nullValue);
        }

        return prov.findValueSerializer(Number.class, property);
    }

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.equals(this.nullValue)) {
            provider.defaultSerializeNull(gen);
        } else {
            dateSerializer.serialize(new Date(value), gen, provider);
        }
    }

}
