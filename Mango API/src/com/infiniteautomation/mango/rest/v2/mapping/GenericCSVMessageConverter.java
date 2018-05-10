/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.TypeUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.infiniteautomation.mango.rest.v2.model.StreamedArrayWithTotal;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Jared Wiltshire
 */
public class GenericCSVMessageConverter extends AbstractJackson2HttpMessageConverter {

    public GenericCSVMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, new MediaType("text", "csv"));
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        try {
            Class<?> serializationView = null;
            FilterProvider filters = null;
            Object value = object;
            JavaType javaType = null;
            if (object instanceof MappingJacksonValue) {
                MappingJacksonValue container = (MappingJacksonValue) object;
                value = container.getValue();
                serializationView = container.getSerializationView();
                filters = container.getFilters();
            }
            if (type != null && value != null && TypeUtils.isAssignable(type, value.getClass())) {
                javaType = getJavaType(type, null);
            }
            ObjectWriter objectWriter;
            if (serializationView != null) {
                objectWriter = this.objectMapper.writerWithView(serializationView);
            }
            else if (filters != null) {
                objectWriter = this.objectMapper.writer(filters);
            }
            else {
                objectWriter = this.objectMapper.writer();
            }
            if (javaType != null && javaType.isContainerType()) {
                objectWriter = objectWriter.forType(javaType);
            }

            if (value instanceof StreamedArrayWithTotal) {
                value = ((StreamedArrayWithTotal) value).getItems();
            }

            JsonNode root;
            try (TokenBuffer generator = new TokenBuffer(this.objectMapper, false)) {
                if (this.objectMapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
                    generator.forceUseOfBigDecimal(true);
                }

                objectWriter.writeValue(generator, value);

                generator.flush();

                JsonParser p = generator.asParser();
                root = this.objectMapper.readTree(p);
                p.close();
            }

            MediaType contentType = outputMessage.getHeaders().getContentType();
            Charset charset = this.charsetForContentType(contentType);

            try (Writer out = new OutputStreamWriter(outputMessage.getBody(), charset)) {
                if (!root.isArray()) {
                    JsonNodeFactory nf = this.objectMapper.getNodeFactory();
                    ArrayNode array = nf.arrayNode(1);
                    array.add(root);
                    root = array;
                }

                CSVWriter csvWriter = new CSVWriter(out);
                writeJsonArray(csvWriter, (ArrayNode) root);
                out.flush();
            }
        }
        catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getOriginalMessage(), ex);
        }
    }

    private void writeJsonArray(CSVWriter csvWriter, ArrayNode root) throws IOException {
        Map<String, Integer> columnPositions = new HashMap<>();
        List<String[]> rows = new ArrayList<>();

        Iterator<JsonNode> it = root.elements();
        while (it.hasNext()) {
            String[] row = jsonNodeToStrings(columnPositions, it.next());
            if (row != null) {
                rows.add(row);
            }
        }

        String[] header = new String[columnPositions.size()];
        for (Entry<String, Integer> entry : columnPositions.entrySet()) {
            header[entry.getValue()] = entry.getKey();
        }
        csvWriter.writeNext(header);

        for (String[] row : rows) {
            csvWriter.writeNext(row);
        }
    }

    private String[] jsonNodeToStrings(Map<String, Integer> columnPositions, JsonNode object) {
        // TODO handle arrays too
        if (!object.isObject()) {
            return null;
        }

        String[] columns = new String[columnPositions.size()];

        Iterator<Entry<String, JsonNode>> it = object.fields();
        while (it.hasNext()) {
            Entry<String, JsonNode> entry = it.next();
            String propertyName = entry.getKey();

            int columnNum = columnPositions.computeIfAbsent(propertyName, name -> columnPositions.size());
            if (columnNum >= columns.length) {
                columns = Arrays.copyOf(columns, columnPositions.size());
            }

            JsonNode value = entry.getValue();
            if (value.isValueNode() && !value.isNull()) {
                columns[columnNum] = value.asText();
            }
        }

        return columns;
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
        try {
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
            reader = reader.forType(javaType);

            MediaType contentType = inputMessage.getHeaders().getContentType();
            Charset charset = this.charsetForContentType(contentType);

            ArrayNode root;
            try (Reader in = new InputStreamReader(inputMessage.getBody(), charset)) {
                CSVReader csvReader = new CSVReader(in);
                root = readCSV(csvReader);
            }

            JsonNode rootNode = root;
            if (!javaType.isCollectionLikeType()) {
                // return the first element
                if (root.size() >= 0) {
                    rootNode = root.get(0);
                } else {
                    return null;
                }
            }
            return reader.readValue(this.objectMapper.treeAsTokens(rootNode));
        }
        catch (JsonProcessingException ex) {
            throw new HttpMessageNotReadableException("JSON parse error: " + ex.getOriginalMessage(), ex);
        }
        catch (IOException ex) {
            throw new HttpMessageNotReadableException("I/O error while reading input message", ex);
        }
    }

    private Charset charsetForContentType(MediaType contentType) {
        if (contentType != null) {
            Charset contentTypeCharset = contentType.getCharset();
            if (contentTypeCharset != null) {
                return contentTypeCharset;
            }
        }
        return StandardCharsets.UTF_8;
    }

    private ArrayNode readCSV(CSVReader reader) throws IOException {
        JsonNodeFactory nf = this.objectMapper.getNodeFactory();
        ArrayNode root = nf.arrayNode();

        String[] header = reader.readNext();
        Map<Integer, String> columnPositions = new HashMap<>();
        int position = 0;
        for (String propertyName : header) {
            columnPositions.put(position++, propertyName);
        }

        String[] row;
        while((row = reader.readNext()) != null) {
            root.add(readCSVRow(nf, columnPositions, row));
        }

        return root;
    }

    private ObjectNode readCSVRow(JsonNodeFactory nf, Map<Integer, String> columnPositions, String[] row) {
        // TODO handle arrays too
        ObjectNode object = nf.objectNode();

        int position = 0;
        for (String value : row) {
            String propertyName = columnPositions.get(position++);
            if (propertyName != null) {
                object.set(propertyName, value == null ? nf.nullNode() : nf.textNode(value));
            }
        }

        return object;
    }
}
