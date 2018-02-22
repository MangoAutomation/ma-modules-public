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
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.DataPointField;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeCsvWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream.StreamContentType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.MultiDataPointStatisticsQuantizerStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointLatestDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointTimeRangeDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.PointValueTimeCacheControl;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
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
            boolean bookend = false;
            boolean useCache = info.isUseCache() != PointValueTimeCacheControl.NONE;
            boolean rendered = info.isUseRendered();
            if(info instanceof ZonedDateTimeRangeQueryInfo)
                bookend = ((ZonedDateTimeRangeQueryInfo)info).isBookend();
            
            if(stream instanceof MultiPointTimeRangeDatabaseStream || stream instanceof MultiPointLatestDatabaseStream) {
                builder.addColumn(PointValueTimeCsvWriter.TIMESTAMP, ColumnType.NUMBER_OR_STRING);
                if(info.isSingleArray()) {
                    if(info.isMultiplePointsPerArray()) {
                        Map<Integer, DataPointVO> voMap = stream.getVoMap();
                        Iterator<Integer> it = voMap.keySet().iterator();
                        while(it.hasNext()) {
                            String xid = voMap.get(it.next()).getXid();
                            builder.addColumn(xid + PointValueTimeCsvWriter.DOT_VALUE, ColumnType.NUMBER_OR_STRING);
                            if(bookend)
                                builder.addColumn(xid + PointValueTimeCsvWriter.DOT_BOOKEND, ColumnType.BOOLEAN);
                            if(useCache)
                                builder.addColumn(xid + PointValueTimeCsvWriter.DOT_CACHED, ColumnType.BOOLEAN);
                            if(rendered)
                                builder.addColumn(xid + PointValueTimeCsvWriter.DOT_RENDERED, ColumnType.STRING);
                            builder.addColumn(xid + PointValueTimeCsvWriter.DOT_ANNOTATION, ColumnType.STRING);
                            for(DataPointField field : info.getExtraFields())
                                builder.addColumn(xid + PointValueTimeCsvWriter.DOT + field.getFieldName(), ColumnType.STRING);
                        }
                    }else {
                        builder.addColumn(PointValueTimeCsvWriter.VALUE, ColumnType.NUMBER_OR_STRING);
                        if(bookend)
                            builder.addColumn(PointValueTimeCsvWriter.BOOKEND, ColumnType.BOOLEAN);
                        if(useCache)
                            builder.addColumn(PointValueTimeCsvWriter.CACHED, ColumnType.BOOLEAN);
                        if(rendered)
                            builder.addColumn(PointValueTimeCsvWriter.RENDERED, ColumnType.STRING);
                        builder.addColumn(PointValueTimeCsvWriter.ANNOTATION, ColumnType.STRING);
                        for(DataPointField field : info.getExtraFields())
                            builder.addColumn(field.getFieldName(), ColumnType.STRING);
                    }
                }else {

                    builder.addColumn(PointValueTimeCsvWriter.XID, ColumnType.STRING);
                    builder.addColumn(PointValueTimeCsvWriter.VALUE, ColumnType.NUMBER_OR_STRING);
                    if(bookend)
                        builder.addColumn(PointValueTimeCsvWriter.BOOKEND, ColumnType.BOOLEAN);
                    if(useCache)
                        builder.addColumn(PointValueTimeCsvWriter.CACHED, ColumnType.BOOLEAN);
                    if(rendered)
                        builder.addColumn(PointValueTimeCsvWriter.RENDERED, ColumnType.STRING);
                    builder.addColumn(PointValueTimeCsvWriter.ANNOTATION, ColumnType.STRING);
                    for(DataPointField field : info.getExtraFields())
                        builder.addColumn(field.getFieldName(), ColumnType.STRING);
                }
            }else if(stream instanceof MultiDataPointStatisticsQuantizerStream) {
                builder.addColumn(PointValueTimeCsvWriter.TIMESTAMP, ColumnType.NUMBER_OR_STRING);
                if(stream.getQueryInfo().isSingleArray()) {
                    if(stream.getQueryInfo().isMultiplePointsPerArray()) {
                        Map<Integer, DataPointVO> voMap = stream.getVoMap();
                        Iterator<Integer> it = voMap.keySet().iterator();
                        
                        while(it.hasNext()) {
                            String xid = voMap.get(it.next()).getXid();
                            if(info.getRollup() == RollupEnum.ALL) {
                                for(RollupEnum rollup : getAllRollups())
                                    builder.addColumn(xid + PointValueTimeCsvWriter.DOT + rollup.name(), ColumnType.NUMBER_OR_STRING);
                            }else {
                                builder.addColumn(xid + PointValueTimeCsvWriter.DOT_VALUE, ColumnType.NUMBER_OR_STRING);
                            }
                            if(rendered)
                                builder.addColumn(xid + PointValueTimeCsvWriter.DOT_RENDERED, ColumnType.STRING);
                            for(DataPointField field : info.getExtraFields())
                                builder.addColumn(xid + PointValueTimeCsvWriter.DOT + field.getFieldName(), ColumnType.STRING);
                        }
                    }else {
                        //Single array
                        if(info.getRollup() == RollupEnum.ALL) {
                            for(RollupEnum rollup : getAllRollups()) {
                                builder.addColumn(rollup.name(), ColumnType.NUMBER_OR_STRING);
                            }
                        }else
                            builder.addColumn(PointValueTimeCsvWriter.VALUE, ColumnType.NUMBER_OR_STRING);
                        if(rendered)
                            builder.addColumn(PointValueTimeCsvWriter.RENDERED, ColumnType.STRING);
                        for(DataPointField field : info.getExtraFields())
                            builder.addColumn(field.getFieldName(), ColumnType.STRING);
                    }
                }else {
                    builder.addColumn(PointValueTimeCsvWriter.XID, ColumnType.STRING);
                    if(info.getRollup() == RollupEnum.ALL) {
                        for(RollupEnum rollup : getAllRollups()) {
                            builder.addColumn(rollup.name(), ColumnType.NUMBER_OR_STRING);
                        }
                    }else
                        builder.addColumn(PointValueTimeCsvWriter.VALUE, ColumnType.NUMBER_OR_STRING);
                    if(rendered)
                        builder.addColumn(PointValueTimeCsvWriter.RENDERED, ColumnType.STRING);
                    for(DataPointField field : info.getExtraFields())
                        builder.addColumn(field.getFieldName(), ColumnType.STRING);
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