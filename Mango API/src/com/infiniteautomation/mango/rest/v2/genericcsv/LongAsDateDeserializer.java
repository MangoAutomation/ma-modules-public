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
public class LongAsDateDeserializer extends StdScalarDeserializer<Long> implements ContextualDeserializer {

    private static final long serialVersionUID = 1L;

    private final JsonDeserializer<Object> dateDeserializer;
    private final Long nullValue;

    public LongAsDateDeserializer() {
        super(Long.class);
        this.dateDeserializer = null;
        this.nullValue = null;
    }

    protected LongAsDateDeserializer(JsonDeserializer<Object> dateDeserializer, Long nullValue) {
        super(Long.class);
        this.dateDeserializer = dateDeserializer;
        this.nullValue = nullValue;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        LongAsDate annotation;
        if (property != null && (annotation = property.getAnnotation(LongAsDate.class)) != null) {
            Long nullValue = null;
            if (annotation.useNullValue()) {
                nullValue = annotation.nullValue();
            }

            JsonDeserializer<Object> dateDeserializer = ctxt.findContextualValueDeserializer(ctxt.constructType(Date.class), property);
            return new LongAsDateDeserializer(dateDeserializer, nullValue);
        }

        return ctxt.findContextualValueDeserializer(ctxt.constructType(Number.class), property);
    }

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Date result = (Date) this.dateDeserializer.deserialize(p, ctxt);

        if (result == null) {
            return this.getNullValue(ctxt);
        }

        return result.getTime();
    }

    @Override
    public Long getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        // this is always going to return null as it doesn't use the contextual deserializer to get the null value
        return this.nullValue;
    }

}
