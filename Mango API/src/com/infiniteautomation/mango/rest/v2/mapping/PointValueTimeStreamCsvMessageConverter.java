/*
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeCsvWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream.StreamContentType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.MultiDataPointStatisticsQuantizerStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointLatestDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointTimeRangeDatabaseStream;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.time.RollupEnum;

/**
 * 
 * @author Jared Wiltshire, Terry Packer
 */
public class PointValueTimeStreamCsvMessageConverter extends AbstractJackson2HttpMessageConverter {
    
    public PointValueTimeStreamCsvMessageConverter() {
        this(new CsvMapper());
    }
    
    public PointValueTimeStreamCsvMessageConverter(CsvMapper csvMapper) {
        super(csvMapper, new MediaType("text", "csv"));
        ((CsvMapper)this.objectMapper).configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true);
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
        if (!canWrite(mediaType))
            return false;
        
        if(MultiPointLatestDatabaseStream.class.isAssignableFrom(clazz) 
                || MultiPointTimeRangeDatabaseStream.class.isAssignableFrom(clazz)
                || MultiDataPointStatisticsQuantizerStream.class.isAssignableFrom(clazz))
            return true;
        else
            return false;
    }
    
    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        MediaType contentType = outputMessage.getHeaders().getContentType();
        JsonEncoding encoding = getJsonEncoding(contentType);
        try {
            PointValueTimeStream<?,?> stream = (PointValueTimeStream<?,?>)object;
            stream.setContentType(StreamContentType.CSV);
            JsonGenerator generator = this.objectMapper.getFactory().createGenerator(outputMessage.getBody(), encoding);
            //Set the schema
            CsvSchema.Builder builder = CsvSchema.builder();
            builder.setUseHeader(true);

            //Setup our rendering parameters
            LatestQueryInfo info = stream.getQueryInfo();
            
            if(stream instanceof MultiPointTimeRangeDatabaseStream || stream instanceof MultiPointLatestDatabaseStream) {
                if(info.isSingleArray()) {
                    if(info.isMultiplePointsPerArray()) {
                        Map<Integer, DataPointVO> voMap = stream.getVoMap();
                        Iterator<Integer> it = voMap.keySet().iterator();
                        boolean firstTimestamp = true;
                        while(it.hasNext()) {
                            String xid = voMap.get(it.next()).getXid();
                            for(PointValueField field : info.getFields()) {
                                if(field == PointValueField.TIMESTAMP) {
                                    if(firstTimestamp)
                                        field.createColumn(builder, xid);
                                    firstTimestamp = false;
                                }else
                                    field.createColumn(builder, xid);
                            }
                        }
                    }else {
                        for(PointValueField field : info.getFields())
                            field.createColumn(builder, null);
                    }
                }else {
                    for(PointValueField field : info.getFields())
                        field.createColumn(builder, null);
                }
            }else if(stream instanceof MultiDataPointStatisticsQuantizerStream) {
                if(stream.getQueryInfo().isSingleArray()) {
                    if(stream.getQueryInfo().isMultiplePointsPerArray()) {
                        Map<Integer, DataPointVO> voMap = stream.getVoMap();
                        Iterator<Integer> it = voMap.keySet().iterator();
                        boolean firstTimestamp = true;
                        while(it.hasNext()) {
                            String xid = voMap.get(it.next()).getXid();
                            for(PointValueField field : info.getFields()) {
                                if(field == PointValueField.TIMESTAMP) {
                                    if(firstTimestamp)
                                        field.createColumn(builder, xid);
                                    firstTimestamp = false;
                                }else if(field == PointValueField.VALUE) {
                                    if(info.getRollup() == RollupEnum.ALL) {
                                        for(RollupEnum rollup : getAllRollups()) {
                                            builder.addColumn(xid + PointValueTimeWriter.DOT + rollup.name(), ColumnType.NUMBER_OR_STRING);
                                        }
                                    }else {
                                        field.createColumn(builder, xid);
                                    }
                                }else {
                                    field.createColumn(builder, xid);
                                }
                            }
                        }
                    }else {
                        //Single array
                        if(info.getRollup() == RollupEnum.ALL) {
                            for(RollupEnum rollup : getAllRollups()) {
                                builder.addColumn(rollup.name(), ColumnType.NUMBER_OR_STRING);
                            }
                            for(PointValueField field : info.getFields()) {
                                if(field == PointValueField.VALUE)
                                    continue;
                                field.createColumn(builder, null);
                            }
                        }else {
                            for(PointValueField field : info.getFields())
                                field.createColumn(builder, null);
                        }
                    }
                }else {
                    if(info.getRollup() == RollupEnum.ALL) {
                        for(RollupEnum rollup : getAllRollups()) {
                            builder.addColumn(rollup.name(), ColumnType.NUMBER_OR_STRING);
                        }
                        for(PointValueField field : info.getFields()) {
                            if(field == PointValueField.VALUE)
                                continue;
                            field.createColumn(builder, null);
                        }
                    }else {
                        for(PointValueField field : info.getFields())
                            field.createColumn(builder, null);   
                    }
                }
            }
            generator.setSchema(builder.build());
            PointValueTimeWriter writer = new PointValueTimeCsvWriter(stream.getQueryInfo(), stream.getVoMap().size(), generator);
            stream.start(writer);
            stream.streamData(writer);
            stream.finish(writer);
            generator.flush();
            
        }
        catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write content: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Helper to get all valid enums for writing
     * TODO Could trim the list based on the data types in the voMap if we wanted
     * @return
     */
    protected RollupEnum[] getAllRollups() {
        List<RollupEnum> enums = new ArrayList<>();
        for(RollupEnum rollup : RollupEnum.values()) {
            switch(rollup) {
                case NONE:
                case FFT:
                case ALL:
                break;
                default:
                    enums.add(rollup);
            }
        }
        return enums.toArray(new RollupEnum[enums.size()]);
    }
}