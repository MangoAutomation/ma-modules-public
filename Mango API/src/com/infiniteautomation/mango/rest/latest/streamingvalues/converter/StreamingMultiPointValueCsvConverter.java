/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import static com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.AbstractStreamMapper.MAPPER_ATTRIBUTE;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

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
@Order(200)
@Component
public class StreamingMultiPointValueCsvConverter extends StreamCsvConverter<StreamingMultiPointModel> {

    @Autowired
    public StreamingMultiPointValueCsvConverter(CsvMapper mapper) {
        super(mapper, StreamingMultiPointModel.class);
    }

    @Override
    protected CsvSchema createSchema(@Nullable Type messageType) {
        var schemaBuilder = CsvSchema.builder()
                .setUseHeader(true)
                .setReorderColumns(true);

        AbstractStreamMapper<?> mapper = (AbstractStreamMapper<?>) Objects.requireNonNull(
                RequestContextHolder.currentRequestAttributes()
                .getAttribute(MAPPER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST));

        Set<PointValueField> fields = mapper.getFields();

        if (fields.contains(PointValueField.TIMESTAMP)) {
            var columnType = mapper.isTimestampFormatted() ? ColumnType.STRING : ColumnType.NUMBER;
            schemaBuilder.addColumn(PointValueField.TIMESTAMP.getFieldName(), columnType);
        }

        for (var point : mapper.getDataPoints().values()) {
            for (var field : fields) {
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
    protected boolean canRead(@Nullable MediaType mediaType) {
        return false;
    }

}