/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.http.converter.HttpMessageConverter;

import com.fasterxml.jackson.databind.SequenceWriter;
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
public class StreamingPointValueCsvConverter extends BaseCsvConverter<Stream<StreamingPointValueTimeModel>> {

    public static final ResolvableType SUPPORTED_TYPE = ResolvableType.forClassWithGenerics(Stream.class, StreamingPointValueTimeModel.class);

    public StreamingPointValueCsvConverter(CsvMapper mapper) {
        super(mapper);
    }

    @Override
    protected CsvSchema createSchema(@Nullable Type type) {
        var fields = fields();

        var schemaBuilder = CsvSchema.builder();
        schemaBuilder.setUseHeader(true);
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
    protected void writeValues(Stream<StreamingPointValueTimeModel> value, SequenceWriter writer) {
        value.forEachOrdered(model -> {
            try {
                writer.write(model);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    protected boolean supportsType(Type type) {
        return SUPPORTED_TYPE.isAssignableFrom(ResolvableType.forType(type));
    }

}