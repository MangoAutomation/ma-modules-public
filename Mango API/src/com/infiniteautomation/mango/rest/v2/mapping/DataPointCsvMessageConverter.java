/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;
import java.lang.reflect.Type;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.infiniteautomation.mango.rest.v2.model.dataPoint.DataPointModel;

/**
 *
 * @author Terry Packer
 */
public class DataPointCsvMessageConverter extends AbstractJackson2HttpMessageConverter {

    private CsvMapper objectMapper;

    public DataPointCsvMessageConverter() {
        this(new CsvMapper());
    }

    public DataPointCsvMessageConverter(CsvMapper csvMapper) {
        super(csvMapper, new MediaType("text", "csv"));
        this.objectMapper = csvMapper;
        this.objectMapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        if (!canRead(mediaType))
            return false;

        return type instanceof Class && DataPointModel.class.isAssignableFrom((Class<?>) type);
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
        try {
            if (inputMessage instanceof MappingJacksonInputMessage) {
                Class<?> deserializationView = ((MappingJacksonInputMessage) inputMessage).getDeserializationView();
                if (deserializationView != null) {
                    return this.objectMapper.readerWithView(deserializationView)
                            .forType(javaType)
                            .with(this.objectMapper.schemaFor(DataPointModel.class))
                            .readValue(inputMessage.getBody());
                }
            }
            return this.objectMapper
                    .readerWithTypedSchemaFor(DataPointModel.class)
                    .readValue(inputMessage.getBody());
        }
        catch (IOException ex) {
            throw new HttpMessageNotReadableException("Could not read document: " + ex.getMessage(), ex);
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#canWrite(java.lang.Class, org.springframework.http.MediaType)
     */
    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        if (!canWrite(mediaType))
            return false;

        if(DataPointModel.class.isAssignableFrom(clazz))
            return true;
        else
            return false;
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        this.objectMapper.writerWithTypedSchemaFor(DataPointModel.class).writeValue(outputMessage.getBody(), object);
    }
}
