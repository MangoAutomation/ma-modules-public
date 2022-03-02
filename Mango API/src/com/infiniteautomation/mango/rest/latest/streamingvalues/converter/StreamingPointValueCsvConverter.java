/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import java.lang.reflect.Type;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;

/**
 * Spring {@link HttpMessageConverter} that converts a {@link Stream} of {@link StreamingPointValueTimeModel} or
 * into CSV format.
 *
 * @author Jared Wiltshire
 */
@Order(0)
@Component
public class StreamingPointValueCsvConverter extends StreamCsvConverter<StreamingPointValueTimeModel> {

    public StreamingPointValueCsvConverter(CsvMapper mapper) {
        super(mapper, StreamingPointValueTimeModel.class);
    }

    @Override
    protected CsvSchema createSchema(@Nullable Type messageType) {
        var fields = fields();

        var schemaBuilder = CsvSchema.builder()
            .setUseHeader(true)
            .setReorderColumns(true);

        if (fields.contains(PointValueField.TIMESTAMP)) {
            schemaBuilder.addColumn(PointValueField.TIMESTAMP.getFieldName(), ColumnType.NUMBER_OR_STRING);
        }
        if (fields.contains(PointValueField.VALUE)) {
            schemaBuilder.addColumn(PointValueField.VALUE.getFieldName(), ColumnType.NUMBER_OR_STRING);
        }
        if (fields.contains(PointValueField.RAW)) {
            schemaBuilder.addColumn(PointValueField.RAW.getFieldName(), ColumnType.NUMBER_OR_STRING);
        }
        if (fields.contains(PointValueField.RENDERED)) {
            schemaBuilder.addColumn(PointValueField.RENDERED.getFieldName(), ColumnType.STRING);
        }
        if (fields.contains(PointValueField.ANNOTATION)) {
            schemaBuilder.addColumn(PointValueField.ANNOTATION.getFieldName(), ColumnType.STRING);
        }
        if (fields.contains(PointValueField.XID)) {
            schemaBuilder.addColumn(PointValueField.XID.getFieldName(), ColumnType.STRING);
        }
        if (fields.contains(PointValueField.NAME)) {
            schemaBuilder.addColumn(PointValueField.NAME.getFieldName(), ColumnType.STRING);
        }
        if (fields.contains(PointValueField.DEVICE_NAME)) {
            schemaBuilder.addColumn(PointValueField.DEVICE_NAME.getFieldName(), ColumnType.STRING);
        }
        if (fields.contains(PointValueField.DATA_SOURCE_NAME)) {
            schemaBuilder.addColumn(PointValueField.DATA_SOURCE_NAME.getFieldName(), ColumnType.STRING);
        }
        return schemaBuilder.build();
    }

    @Override
    protected boolean canRead(@Nullable MediaType mediaType) {
        return false;
    }

}