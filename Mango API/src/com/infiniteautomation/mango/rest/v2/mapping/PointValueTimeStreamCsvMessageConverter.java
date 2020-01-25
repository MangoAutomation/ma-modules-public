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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.core.ResolvableType;
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
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeCsvWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream.StreamContentType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.RollupEnum;
import com.infiniteautomation.mango.rest.v2.model.pointValue.XidPointValueTimeModel;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.MultiDataPointDefaultRollupStatisticsQuantizerStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.quantize.MultiDataPointStatisticsQuantizerStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointLatestDatabaseStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.MultiPointTimeRangeDatabaseStream;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.MediaTypes;


/**
 * Message convert to read/write CSV point value data in a streaming way
 *
 * @author Jared Wiltshire, Terry Packer
 */
public class PointValueTimeStreamCsvMessageConverter extends AbstractJackson2HttpMessageConverter {

    public PointValueTimeStreamCsvMessageConverter(CsvMapper csvMapper) {
        super(csvMapper, MediaTypes.CSV_V1);
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        if (!canRead(mediaType))
            return false;

        //Check to make sure this is a stream of XidPointValueTimeModel objects
        ResolvableType resolvedType = ResolvableType.forType(type);
        if(resolvedType.getRawClass() instanceof Class && Stream.class.isAssignableFrom(resolvedType.getRawClass())) {
            ResolvableType streamType = resolvedType.getGeneric(0);
            return streamType.getType() instanceof Class && XidPointValueTimeModel.class.isAssignableFrom((Class<?>) streamType.getType());
        }else {
            return false;
        }
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

        //TODO detect type/schema for the callback
        // could use the type if we add generics to PointValueTimeImportStream
        reader = reader.forType(XidPointValueTimeModel.class);

        CsvSchema schema = CsvSchema.emptySchema().withHeader().withStrictHeaders(false);
        ObjectReader csvReader = reader.with(schema);
        try {
            MappingIterator<XidPointValueTimeModel> iterator = csvReader.readValues(inputMessage.getBody());
            CsvSchema fullSchema = (CsvSchema)iterator.getParser().getSchema();
            if(fullSchema.column(PointValueField.VALUE.getFieldName()) == null) {
                throw new IOException("Missing required column " + PointValueField.VALUE.getFieldName());
            }else if(fullSchema.column(PointValueField.XID.getFieldName()) == null) {
                throw new IOException("Missing required column " + PointValueField.XID.getFieldName());
            }else if(fullSchema.column(PointValueField.TIMESTAMP.getFieldName()) == null) {
                throw new IOException("Missing required column " + PointValueField.TIMESTAMP.getFieldName());
            }
            fullSchema.column("xid");
            Stream<XidPointValueTimeModel> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
            return stream;
        }catch (IOException ex) {
            throw new HttpMessageNotReadableException("Could not read document: " + ex.getMessage(), ex, inputMessage);
        }
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        if (!canWrite(mediaType))
            return false;

        if(MultiPointLatestDatabaseStream.class.isAssignableFrom(clazz)
                || MultiPointTimeRangeDatabaseStream.class.isAssignableFrom(clazz)
                || MultiDataPointStatisticsQuantizerStream.class.isAssignableFrom(clazz)
                || MultiDataPointDefaultRollupStatisticsQuantizerStream.class.isAssignableFrom(clazz))
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
            }else if(stream instanceof MultiDataPointStatisticsQuantizerStream || stream instanceof MultiDataPointDefaultRollupStatisticsQuantizerStream) {
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