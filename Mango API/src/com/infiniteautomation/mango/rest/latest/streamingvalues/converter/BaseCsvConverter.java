/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import static com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.AbstractStreamMapper.REQUEST_ATTRIBUTE_NAME;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.AbstractStreamMapper;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.web.MediaTypes;

/**
 * @author Jared Wiltshire
 */
public abstract class BaseCsvConverter<T> extends AbstractGenericHttpMessageConverter<T>  {

    protected final CsvMapper mapper;

    public BaseCsvConverter(CsvMapper mapper) {
        super(MediaTypes.CSV_V1);
        this.mapper = mapper;
    }

    protected Charset resolveCharset(HttpHeaders headers) {
        Charset charset = null;
        MediaType contentType = headers.getContentType();
        if (contentType != null) {
            charset = contentType.getCharset();
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        return charset;
    }

    @Override
    public T read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        // this should not be called... org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor will call the method with Type
        // if instance of GenericHttpMessageConverter
        throw new UnsupportedOperationException();
    }

    protected abstract CsvSchema createSchema(@Nullable Type type);

    protected abstract void writeValues(T value, SequenceWriter writer);

    @Override
    protected void writeInternal(T value, @Nullable Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        Charset charset = resolveCharset(outputMessage.getHeaders());

        try (var writer = new OutputStreamWriter(outputMessage.getBody(), charset);
             var sequenceWriter = mapper.writer()
                     .with(createSchema(type))
                     .writeValues(writer)) {

            writeValues(value, sequenceWriter);
        }
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return supportsType(clazz);
    }

    protected abstract boolean supportsType(Type type);

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return supportsType(type) && canRead(mediaType);
    }

    @Override
    public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
        return supportsType(type != null ? type : clazz) && canWrite(mediaType);
    }

    protected ColumnType columnTypeForDataType(DataType dataType) {
        switch (dataType) {
            case BINARY: return ColumnType.BOOLEAN;
            case MULTISTATE:
            case NUMERIC: return ColumnType.NUMBER;
            case ALPHANUMERIC: return ColumnType.STRING;
        }
        throw new IllegalArgumentException("Unknown data type: " + dataType);
    }

    protected Set<PointValueField> fields() {
        AbstractStreamMapper<?> mapper = (AbstractStreamMapper<?>) Objects.requireNonNull(
                RequestContextHolder.currentRequestAttributes()
                        .getAttribute(REQUEST_ATTRIBUTE_NAME, RequestAttributes.SCOPE_REQUEST));
        return mapper.getFields();
    }
}
