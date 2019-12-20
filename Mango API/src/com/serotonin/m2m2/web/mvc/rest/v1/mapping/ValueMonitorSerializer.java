/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.web.mvc.rest.v1.mapping;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.infiniteautomation.mango.monitor.ValueMonitor;
import com.serotonin.m2m2.Common;

/**
 *
 * @author Terry Packer
 */
public class ValueMonitorSerializer<T> extends JsonSerializer<ValueMonitor<T>>{

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String TYPE = "modelType";

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(ValueMonitor<T> monitor, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeStartObject();
        jgen.writeStringField(ID, monitor.getId());
        jgen.writeStringField(NAME, monitor.getName().translate(Common.getTranslations()));

        T value = monitor.getValue();

        if (value == null) {
            jgen.writeNullField(VALUE);
        } else if (value instanceof Double) {
            jgen.writeNumberField(VALUE, (Double) value);
        } else if (value instanceof Float) {
            jgen.writeNumberField(VALUE, (Float) value);
        } else if (value instanceof Long) {
            jgen.writeNumberField(VALUE, (Long) value);
        } else if (value instanceof Integer) {
            jgen.writeNumberField(VALUE, (Integer) value);
        } else if (value instanceof AtomicInteger) {
            jgen.writeNumberField(VALUE, ((AtomicInteger) value).get());
        } else if (value instanceof Boolean) {
            jgen.writeBooleanField(VALUE, (Boolean) value);
        } else if (value instanceof String) {
            jgen.writeStringField(VALUE, (String) value);
        } else {
            jgen.writeStringField(VALUE, value.toString());
        }

        jgen.writeStringField(TYPE, value.getClass().getSimpleName());
        jgen.writeEndObject();

    }

}
