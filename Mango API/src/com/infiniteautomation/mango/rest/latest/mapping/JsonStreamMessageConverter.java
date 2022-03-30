/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.mapping;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jdk8.StreamSerializer;
import com.infiniteautomation.mango.rest.latest.PointValueModificationRestController;
import com.infiniteautomation.mango.spring.annotations.RestMapper;

/**
 * Generic converter from JSON to {@link Stream} using {@link ObjectMapper Jackson Mapper}.
 * This converter does not support writing as the standard {@link MappingJackson2HttpMessageConverter} supports
 * writing JSON from a Stream using {@link StreamSerializer}.
 *
 * <p>This converter is used when importing point values via {@link PointValueModificationRestController}.</p>
 *
 * @author Terry Packer
 */
@Component
public class JsonStreamMessageConverter extends MappingJackson2HttpMessageConverter {

    private static final ResolvableType SUPPORTED_TYPE = ResolvableType.forClass(Stream.class);

    @Autowired
    public JsonStreamMessageConverter(@RestMapper ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        if (!canRead(mediaType))
            return false;

        ResolvableType resolvedType = ResolvableType.forType(type);
        return SUPPORTED_TYPE.isAssignableFrom(resolvedType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
        // writing JSON streams can be handled by Jackson using the standard MappingJackson2HttpMessageConverter
        return false;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        JavaType javaType = getJavaType(clazz, null);
        return readJavaType(javaType, inputMessage);
    }

    @Override
    public Object read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        JavaType javaType = getJavaType(type, contextClass);
        return readJavaType(javaType, inputMessage);
    }

    private Object readJavaType(JavaType javaType, HttpInputMessage inputMessage) {
        Class<?> deserializationView = null;
        if (inputMessage instanceof MappingJacksonInputMessage) {
            deserializationView = ((MappingJacksonInputMessage) inputMessage).getDeserializationView();
        }

        ObjectReader reader;
        if (deserializationView != null) {
            reader = getObjectMapper().readerWithView(deserializationView);
        } else {
            reader = getObjectMapper().reader();
        }
        reader = reader.forType(javaType.containedType(0));

        try {
            var iterator = reader.readValues(inputMessage.getBody());
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
        } catch (IOException ex) {
            throw new HttpMessageNotReadableException("Could not read document", ex, inputMessage);
        }
    }
}
