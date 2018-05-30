/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.genericcsv;

import java.io.IOException;
import java.sql.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * @author Jared Wiltshire
 */
public class LongAsDateDeserializer extends StdScalarDeserializer<Date> implements ContextualDeserializer {

    private static final long serialVersionUID = 1L;

    private final JsonDeserializer<Object> numberDeserializer;
    private final Long nullValue;

    public LongAsDateDeserializer() {
        super(Long.class);
        this.numberDeserializer = null;
        this.nullValue = null;
    }

    public LongAsDateDeserializer(JsonDeserializer<Object> numberDeserializer, Long nullValue) {
        super(Long.class);
        this.numberDeserializer = numberDeserializer;
        this.nullValue = nullValue;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<Object> numberDeserializer = ctxt.findContextualValueDeserializer(ctxt.constructType(Number.class), property);

        LongAsDate annotation;
        if (property != null && (annotation = property.getAnnotation(LongAsDate.class)) != null) {
            Long nullValue = null;
            if (annotation.useNullValue()) {
                nullValue = annotation.nullValue();
            }

            return new LongAsDateDeserializer(numberDeserializer, nullValue);
        }

        return numberDeserializer;
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Long result = (Long) this.numberDeserializer.deserialize(p, ctxt);

        if (result == null || result.equals(this.nullValue)) {
            return null;
        }

        return new Date(result);
    }
}
