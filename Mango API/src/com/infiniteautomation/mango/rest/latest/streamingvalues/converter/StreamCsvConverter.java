/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import java.lang.reflect.Type;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.ResolvableType;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

/**
 * @author Jared Wiltshire
 */
public abstract class StreamCsvConverter<T> extends BaseCsvConverter<Stream<T>, T> {

    public StreamCsvConverter(CsvMapper mapper, Class<T> rowType) {
        super(mapper, rowType);
    }

    @Override
    protected Stream<T> writeRows(Stream<T> message) {
        return message;
    }

    @Override
    protected Stream<T> readRows(Stream<T> rows) {
        return rows;
    }

    @Override
    protected CsvSchema createSchema(@Nullable Type messageType) {
        return mapper.schemaFor(rowType).withHeader().withColumnReordering(true);
    }

    @Override
    protected boolean supportsType(Type type) {
        var supportedType = ResolvableType.forClassWithGenerics(Stream.class, rowType);
        return supportedType.isAssignableFrom(ResolvableType.forType(type));
    }
}
