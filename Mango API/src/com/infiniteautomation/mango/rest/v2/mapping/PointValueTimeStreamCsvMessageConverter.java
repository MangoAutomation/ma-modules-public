/*
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointTimeRangeDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeJsonWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeWriter;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * @author Jared Wiltshire
 */
public class PointValueTimeStreamCsvMessageConverter extends AbstractJackson2HttpMessageConverter {

    CsvMapper csvMapper;
    
    public PointValueTimeStreamCsvMessageConverter() {
        this(new CsvMapper());
    }
    
    public PointValueTimeStreamCsvMessageConverter(CsvMapper csvMapper) {
        super(csvMapper, new MediaType("text", "csv"));
        this.csvMapper = csvMapper;
    }

    /* (non-Javadoc)
     * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#canRead(java.lang.Class, org.springframework.http.MediaType)
     */
    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        if (!canRead(mediaType))
            return false;
        
        if(PointValueTimeStream.class.isAssignableFrom(clazz))
            return true;
        else
            return false;
                    
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
                            .with(CsvSchema.emptySchema().withHeader())
                            .readValue(inputMessage.getBody());
                }
            }
            return this.objectMapper.reader()
                    .forType(javaType)
                    .with(CsvSchema.emptySchema().withHeader())
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
        if (!canRead(mediaType))
            return false;
        
        if(PointValueTimeStream.class.isAssignableFrom(clazz))
            return true;
        else
            return false;
    }
    
    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        
        MediaType contentType = outputMessage.getHeaders().getContentType();
        JsonEncoding encoding = getJsonEncoding(contentType);
        JsonGenerator generator = this.objectMapper.getFactory().createGenerator(outputMessage.getBody(), encoding);
        try {
            PointValueTimeStream<?,?> stream = (PointValueTimeStream<?,?>)object;
            //Set the schema
            if(stream instanceof MultiPointTimeRangeDatabaseStream) {
                
                CsvSchema.Builder builder = CsvSchema.builder();
                builder.setUseHeader(true);
                
                if(stream.getQueryInfo().isSingleArray()) {
                    if(stream.getQueryInfo().isMultiplePointsPerArray()) {
                        //TODO Logic for Rendered, RenderedAndRaw
                        Map<Integer, DataPointVO> voMap = stream.getVoMap();
                        Iterator<Integer> it = voMap.keySet().iterator();
                        while(it.hasNext()) {
                            builder.addColumn(voMap.get(it.next()).getXid(), ColumnType.NUMBER_OR_STRING);
                        }
                    }else {
                        builder.addColumn("value", ColumnType.NUMBER_OR_STRING);
                    }
                    builder.addColumn("timestamp", ColumnType.NUMBER_OR_STRING);
                    builder.addColumn("annotation", ColumnType.STRING);
                    builder.addColumn("bookend", ColumnType.BOOLEAN);
                }else {
                    if(stream.getQueryInfo().isMultiplePointsPerArray()) {
                        //TODO Logic for Rendered, RenderedAndRaw
                        Map<Integer, DataPointVO> voMap = stream.getVoMap();
                        Iterator<Integer> it = voMap.keySet().iterator();
                        while(it.hasNext()) {
                            builder.addColumn(voMap.get(it.next()).getXid(), ColumnType.NUMBER_OR_STRING);
                        }
                    }else {
                        builder.addColumn("value", ColumnType.NUMBER_OR_STRING);
                    }
                    builder.addColumn("timestamp", ColumnType.NUMBER_OR_STRING);
                    builder.addColumn("annotation", ColumnType.STRING);
                    builder.addColumn("bookend", ColumnType.BOOLEAN);
                }
                generator.setSchema(builder.build());
            }
            PointValueTimeWriter writer = new PointValueTimeJsonWriter(stream.getQueryInfo(), generator);
            stream.start(writer);
            stream.streamData(writer);
            stream.finish(writer);
            
//            writePrefix(generator, object);
//
//            Class<?> serializationView = null;
//            FilterProvider filters = null;
//            Object value = object;
//            JavaType javaType = null;
//            if (object instanceof MappingJacksonValue) {
//                MappingJacksonValue container = (MappingJacksonValue) object;
//                value = container.getValue();
//                serializationView = container.getSerializationView();
//                filters = container.getFilters();
//            }
//            if (type != null && value != null && TypeUtils.isAssignable(type, value.getClass())) {
//                javaType = getJavaType(type, null);
//            }
//            ObjectWriter objectWriter;
//            if (serializationView != null) {
//                objectWriter = this.objectMapper.writerWithView(serializationView);
//            }
//            else if (filters != null) {
//                objectWriter = this.objectMapper.writer(filters);
//            }
//            else {
//                objectWriter = this.objectMapper.writer();
//            }
//
//            if (javaType != null && javaType.isContainerType()) {
//                objectWriter = objectWriter.forType(javaType);
//            }
//
//            if (javaType != null) {
//                CsvSchema schema = csvMapper.schemaFor(javaType).withHeader();
//                objectWriter = objectWriter.with(schema);
//            }
//            
////            SerializationConfig config = objectWriter.getConfig();
////            if (contentType != null && contentType.isCompatibleWith(TEXT_EVENT_STREAM) &&
////                    config.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
////                objectWriter = objectWriter.with(this.ssePrettyPrinter);
////            }
//            objectWriter.writeValue(generator, value);
//
//            writeSuffix(generator, object);
            generator.flush();

        }
        catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write content: " + ex.getMessage(), ex);
        }
    }
}