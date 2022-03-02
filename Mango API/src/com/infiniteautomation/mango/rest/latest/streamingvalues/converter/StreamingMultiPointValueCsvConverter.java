/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import static com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.AbstractStreamMapper.REQUEST_ATTRIBUTE_NAME;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.AbstractStreamMapper;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingMultiPointModel;

/**
 * Spring {@link HttpMessageConverter} that converts a {@link Stream} of {@link StreamingMultiPointModel}
 * into CSV format.
 *
 * @author Jared Wiltshire
 */
@Component
public class StreamingMultiPointValueCsvConverter extends BaseCsvConverter<Stream<StreamingMultiPointModel>> {

    public static final ResolvableType SUPPORTED_TYPE = ResolvableType.forClassWithGenerics(Stream.class, StreamingMultiPointModel.class);

    @Autowired
    public StreamingMultiPointValueCsvConverter(CsvMapper mapper) {
        super(mapper);
    }

    @Override
    protected CsvSchema createSchema(@Nullable Type type) {
        var schemaBuilder = CsvSchema.builder();
        schemaBuilder.setUseHeader(true);

        AbstractStreamMapper<?> mapper = (AbstractStreamMapper<?>) Objects.requireNonNull(
                RequestContextHolder.currentRequestAttributes()
                .getAttribute(REQUEST_ATTRIBUTE_NAME, RequestAttributes.SCOPE_REQUEST));

        if (mapper.getFields().contains(PointValueField.TIMESTAMP)) {
            var columnType = mapper.getDateTimeFormatter() == null ? ColumnType.NUMBER : ColumnType.STRING;
            schemaBuilder.addColumn(PointValueField.TIMESTAMP.getFieldName(), columnType);
        }

        for (var point : mapper.getDataPoints().values()) {
            for (var field : mapper.getFields()) {
                if (field == PointValueField.VALUE) {
                    // value is put into XID column with no suffix
                    var dataType = point.getPointLocator().getDataType();
                    schemaBuilder.addColumn(point.getXid(), columnTypeForDataType(dataType));
                } else if (field != PointValueField.TIMESTAMP) {
                    // timestamp is not repeated for each point
                    schemaBuilder.addColumn(point.getXid() + "." + field.getFieldName(), ColumnType.NUMBER_OR_STRING);
                }
            }
        }

        return schemaBuilder.build();
    }

    @Override
    protected void writeValues(Stream<StreamingMultiPointModel> value, SequenceWriter writer) {
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

    @Override
    protected boolean canRead(@Nullable MediaType mediaType) {
        return false;
    }

}