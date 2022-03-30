/*
 * Copyright (C) 2022 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.streamingvalues.converter;

import static com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.AbstractStreamMapper.MAPPER_ATTRIBUTE;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.ColumnType;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.streamingvalues.mapper.AbstractStreamMapper;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.web.MediaTypes;

/**
 * @param <T> message type
 * @param <R> CSV row type
 * @author Jared Wiltshire
 */
public abstract class BaseCsvConverter<T, R> extends AbstractGenericHttpMessageConverter<T> {

    protected final CsvMapper mapper;
    protected final Class<R> rowType;

    public BaseCsvConverter(CsvMapper mapper, Class<R> rowType) {
        super(MediaTypes.CSV_V2);
        this.mapper = mapper;
        this.rowType = rowType;
    }

    /**
     * Creates a CsvSchema for a specific message type, used for reading and writing.
     *
     * @param messageType message type
     * @return schema for the row type contained in the message
     */
    protected abstract CsvSchema createSchema(@Nullable Type messageType);

    /**
     * Extracts rows from the message for writing CSV.
     *
     * @param message message returned from REST controller
     * @return stream of row values to be written
     */
    protected abstract Stream<R> writeRows(T message);

    /**
     * Converts rows into a message for reading CSV.
     *
     * @param rows stream of row values read from CSV
     * @return message to be passed to REST controller
     */
    protected abstract T readRows(Stream<R> rows);

    /**
     * @param type message type
     * @return true if converter can read/write this type
     */
    protected abstract boolean supportsType(Type type);

    @Override
    public T read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        Charset charset = resolveCharset(inputMessage.getHeaders());
        var messageType = mapper.getTypeFactory().constructType(rowType);

        var reader = new InputStreamReader(inputMessage.getBody(), charset);
        MappingIterator<R> iterator = mapper.readerFor(messageType)
                .with(createSchema(type))
                .readValues(reader);

        var spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL);
        var stream = StreamSupport.stream(spliterator, false);
        return readRows(stream);
    }

    @Override
    protected T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        // this should not be called... org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor will call the method with Type
        // if instance of GenericHttpMessageConverter
        throw new UnsupportedOperationException();
    }

    @Override
    protected void writeInternal(T value, @Nullable Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try {
            Charset charset = resolveCharset(outputMessage.getHeaders());
            var messageType = mapper.getTypeFactory().constructType(rowType);

            var writer = new OutputStreamWriter(outputMessage.getBody(), charset);
            var sequenceWriter = mapper.writerFor(messageType)
                    .with(createSchema(type))
                    .writeValues(writer);

            // close the stream after writing
            try (var stream = writeRows(value)) {
                stream.forEachOrdered(r -> {
                    try {
                        sequenceWriter.write(r);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        } finally {
            // ensure the message is closed after writing, value may be a stream from the database layer for example
            if (value instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) value).close();
                } catch (Exception e) {
                    logger.warn("Failed to close output message", e);
                }
            }
        }
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return supportsType(clazz);
    }

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
            case BINARY:
                return ColumnType.BOOLEAN;
            case MULTISTATE:
            case NUMERIC:
                return ColumnType.NUMBER;
            case ALPHANUMERIC:
                return ColumnType.STRING;
        }
        throw new IllegalArgumentException("Unknown data type: " + dataType);
    }

    protected Set<PointValueField> fields() {
        AbstractStreamMapper<?> mapper = (AbstractStreamMapper<?>) Objects.requireNonNull(
                RequestContextHolder.currentRequestAttributes()
                        .getAttribute(MAPPER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST));
        return mapper.getFields();
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

}
