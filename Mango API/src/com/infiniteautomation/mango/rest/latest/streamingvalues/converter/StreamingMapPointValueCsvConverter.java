/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;

public class StreamingMapPointValueCsvConverter extends BaseCsvConverter<Map<String, Stream<StreamingPointValueTimeModel>>> {

    public static final ResolvableType SUPPORTED_TYPE = ResolvableType.forClassWithGenerics(Map.class,
            ResolvableType.forClass(String.class),
            ResolvableType.forClassWithGenerics(Stream.class, StreamingPointValueTimeModel.class));

    private final StreamingPointValueCsvConverter delegate;

    public StreamingMapPointValueCsvConverter(StreamingPointValueCsvConverter delegate) {
        super(delegate.mapper);
        this.delegate = delegate;
    }

    @Override
    protected CsvSchema createSchema(@Nullable Type type) {
        return delegate.createSchema(type);
    }

    @Override
    protected void writeValues(Map<String, Stream<StreamingPointValueTimeModel>> value, SequenceWriter writer) {
        for (var stream : value.values()) {
            stream.forEachOrdered(model -> {
                try {
                    writer.write(model);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
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