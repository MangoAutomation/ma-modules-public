/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;

/**
 * @author Jared Wiltshire
 */
@Order(0)
@Component
public class StreamingMapPointValueCsvConverter extends BaseCsvConverter<Map<String, Stream<StreamingPointValueTimeModel>>, StreamingPointValueTimeModel> {

    public static final ResolvableType SUPPORTED_TYPE = ResolvableType.forClassWithGenerics(Map.class,
            ResolvableType.forClass(String.class),
            ResolvableType.forClassWithGenerics(Stream.class, StreamingPointValueTimeModel.class));

    private final StreamingPointValueCsvConverter delegate;

    @Autowired
    public StreamingMapPointValueCsvConverter(CsvMapper mapper, StreamingPointValueCsvConverter delegate) {
        super(mapper, StreamingPointValueTimeModel.class);
        this.delegate = delegate;
    }

    @Override
    protected CsvSchema createSchema(@Nullable Type messageType) {
        return delegate.createSchema(messageType);
    }

    @Override
    protected Stream<StreamingPointValueTimeModel> writeRows(Map<String, Stream<StreamingPointValueTimeModel>> message) {
        return message.values().stream().flatMap(Function.identity());
    }

    @Override
    protected Map<String, Stream<StreamingPointValueTimeModel>> readRows(Stream<StreamingPointValueTimeModel> rows) {
        throw new UnsupportedOperationException();
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