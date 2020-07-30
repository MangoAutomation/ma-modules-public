/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.mapping;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * Generic converter for JSON Streams
 * 
 * TODO Support writing
 * @author Terry Packer
 *
 */
public class JsonStreamMessageConverter extends MappingJackson2HttpMessageConverter {

    public JsonStreamMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    
    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        if (!canRead(mediaType))
            return false;

        //Check to make sure this is a stream of XidPointValueTimeModel objects
        ResolvableType resolvedType = ResolvableType.forType(type);
        return resolvedType.getRawClass() instanceof Class && Stream.class.isAssignableFrom(resolvedType.getRawClass());
    }
    
    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        JavaType javaType = getJavaType(clazz, null);
        return readJavaType(javaType, inputMessage);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
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
            reader = this.objectMapper.readerWithView(deserializationView);
        } else {
            reader = this.objectMapper.reader();
        }
        reader = reader.forType(javaType.containedType(0));
        try {
            Stream<?> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(reader.readValues(inputMessage.getBody()), Spliterator.ORDERED), false); 
            return stream;
        }catch (IOException ex) {
            throw new HttpMessageNotReadableException("Could not read document: " + ex.getMessage(), ex, inputMessage);
        }

    }
    
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }
}
