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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
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
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryDataPageStream;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Jared Wiltshire
 */
public class GenericCSVMessageConverter extends AbstractJackson2HttpMessageConverter {

    public static final String NULL_STRING = "NULL";
    public static final String ARRAY_STRING = "ARRAY";
    public static final String OBJECT_STRING = "OBJECT";
    public static final String EMPTY_STRING = "EMPTY";

    // Excel converts true -> TRUE, false -> FALSE when reading CSV for some reason
    // true, false, True and False are handled by Jackson
    // only used when parsing the CSV
    public static final String TRUE_STRING = "TRUE";
    public static final String FALSE_STRING = "FALSE";

    public static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");

    private final JsonNodeFactory nodeFactory;

    public GenericCSVMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaTypes.CSV_V2);
        this.nodeFactory = objectMapper.getNodeFactory();
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

            // TODO enable writer features/formats
            // e.g. Dates encoded as excel compatible date format

            if (javaType != null && javaType.isContainerType()) {
                objectWriter = objectWriter.forType(javaType);
            }

            // special handling for our streamed arrays with total, unwrap the array and discard the total
            if (value instanceof StreamedArrayWithTotal) {
                value = ((StreamedArrayWithTotal) value).getItems();
            }
            if (value instanceof QueryDataPageStream) {
                // force using the serializer for QueryArrayStream so the total isn't written out
                objectWriter = objectWriter.forType(QueryArrayStream.class);
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
                if (charset.name().startsWith("UTF-")) {
                    // write the UTF BOM (byte order mark)
                    out.write('\ufeff');
                }

                if (!root.isArray()) {
                    ArrayNode array = this.nodeFactory.arrayNode(1);
                    array.add(root);
                    root = array;
                }

                CSVWriter csvWriter = new CSVWriter(out);
                writeCSV(csvWriter, (ArrayNode) root);
                out.flush();
            }
        }
        catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getOriginalMessage(), ex);
        }
    }

    /**
     * Write the CSV representation of the JSON tree out
     *
     * @param csvWriter
     * @param root
     * @throws IOException
     */
    private void writeCSV(CSVWriter csvWriter, ArrayNode root) throws IOException {
        Map<String, Integer> columnPositions = new HashMap<>();
        List<List<String>> rows = new ArrayList<>();

        int maxRowSize = 0;
        Iterator<JsonNode> it = root.elements();
        while (it.hasNext()) {
            JsonNode node = it.next();

            List<String> row = createCSVRow(columnPositions, node);
            if (row.size() > maxRowSize) {
                maxRowSize = row.size();
            }
            rows.add(row);
        }

        String[] header = new String[columnPositions.size()];
        for (Entry<String, Integer> entry : columnPositions.entrySet()) {
            header[entry.getValue()] = entry.getKey();
        }
        csvWriter.writeNext(header);

        for (List<String> row : rows) {
            if (row.size() < maxRowSize) {
                this.expandList(row, maxRowSize);
            }
            csvWriter.writeNext(row.toArray(new String[row.size()]));
        }
    }

    /**
     * Create a CSV row for each JsonNode in the root array
     *
     * @param columnPositions
     * @param node
     * @return
     */
    private List<String> createCSVRow(Map<String, Integer> columnPositions, JsonNode node) {
        int numColumns = columnPositions.size();
        List<String> columns = new ArrayList<>(numColumns);
        this.expandList(columns, numColumns);

        this.traverseAndSetColumns(columnPositions, columns, node, "");
        return columns;
    }

    /**
     * Recursively traverse the JSON tree structure and set the columns in the CSV accordingly.
     *
     * @param columnPositions
     * @param columns
     * @param node
     * @param path
     */
    private void traverseAndSetColumns(Map<String, Integer> columnPositions, List<String> columns, JsonNode node, String path) {
        int columnNum = columnPositions.computeIfAbsent(path, p -> columnPositions.size());
        this.expandList(columns, columnNum + 1);

        if (node.isObject()) {
            columns.set(columnNum, OBJECT_STRING);

            Iterator<Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Entry<String, JsonNode> entry = it.next();
                JsonNode value = entry.getValue();
                String propertyName = entry.getKey();
                this.traverseAndSetColumns(columnPositions, columns, value, path.isEmpty() ? propertyName : path + "/" + propertyName);
            }
        } else if (node.isArray()) {
            columns.set(columnNum, ARRAY_STRING);

            Iterator<JsonNode> it = node.elements();
            int i = 0;
            while (it.hasNext()) {
                JsonNode value = it.next();
                String propertyName = Integer.toString(i++);
                this.traverseAndSetColumns(columnPositions, columns, value, path.isEmpty() ? propertyName : path + "/" + propertyName);
            }
        } else if (node.isValueNode()) {
            if (node.isNull()) {
                // we can't set the column to null as its encoded as an empty string, use our designated NULL string instead
                columns.set(columnNum, NULL_STRING);
            } else if (node.isTextual() && node.asText().isEmpty()) {
                // we can't set the column to null as its encoded as an empty string, use our designated NULL string instead
                columns.set(columnNum, EMPTY_STRING);
            } else {
                columns.set(columnNum, node.asText());
            }
        }
    }

    /**
     * Expand a list to the given size, filling it with null (encoded as empty string)
     *
     * @param list
     * @param size
     */
    private void expandList(List<String> list, int size) {
        while (list.size() < size) {
            list.add(null);
        }
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

            // TODO enable reader features/formats
            // e.g. Dates encoded as excel compatible date format

            reader = reader.forType(javaType);

            MediaType contentType = inputMessage.getHeaders().getContentType();
            Charset charset = this.charsetForContentType(contentType);

            ArrayNode root;
            try (BOMInputStream is = new BOMInputStream(inputMessage.getBody(), false, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE,
                    ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE)) {

                String charsetName = is.getBOMCharsetName();
                if (charsetName != null) {
                    charset = Charset.forName(charsetName);
                }

                try (Reader in = new InputStreamReader(is, charset)) {
                    CSVReader csvReader = new CSVReader(in);
                    root = readCSV(csvReader);
                }
            }

            JsonNode rootNode = root;
            
            if (root.size() == 1 && !(
                    javaType.isCollectionLikeType() ||
                    ArrayNode.class.isAssignableFrom(javaType.getRawClass()))) {
                rootNode = root.get(0);
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

    /**
     * Build an ArrayNode from the lines read from the CSV
     *
     * @param reader
     * @return
     * @throws IOException
     */
    private ArrayNode readCSV(CSVReader reader) throws IOException {
        ArrayNode root = this.nodeFactory.arrayNode();

        String[] header = reader.readNext();
        Map<Integer, String> columnPositions = new HashMap<>();
        int position = 0;
        for (String propertyName : header) {
            columnPositions.put(position++, propertyName);
        }

        String[] row;
        while((row = reader.readNext()) != null) {
            root.add(readCSVRow(columnPositions, row));
        }

        return root;
    }

    private static enum ObjectType {
        OBJECT, ARRAY, NOT_SPECIFIED
    }

    /**
     * Create a Json object or array from a CSV row. All values will be string values
     * or null values. We rely on Jackson to interpret these string values as the correct type
     * when converting the tree to the model object.
     *
     * @param columnPositions
     * @param row
     * @return
     */
    private JsonNode readCSVRow(Map<Integer, String> columnPositions, String[] row) {
        ObjectNode object = this.nodeFactory.objectNode();

        Map<String, ObjectType> objectTypes = new HashMap<>();

        for (int i = 0; i < row.length; i++) {
            String value = row[i];
            String path = columnPositions.get(i);

            // empty header row, assume path is the root object
            if (i == 0 && path == null && columnPositions.isEmpty()) {
                path = "";
            }

            if (path == null || value == null || value.isEmpty()) {
                // no header for the column, or value was undefined, do nothing
            } else if (OBJECT_STRING.equals(value)) {
                objectTypes.put(path, ObjectType.OBJECT);
            } else if (ARRAY_STRING.equals(value)) {
                objectTypes.put(path, ObjectType.ARRAY);
            } else {
                JsonNode valueNode = null;
                if (NULL_STRING.equals(value)) {
                    valueNode = this.nodeFactory.nullNode();
                } else if (TRUE_STRING.equals(value)) {
                    valueNode = this.nodeFactory.booleanNode(true);
                } else if (FALSE_STRING.equals(value)) {
                    valueNode = this.nodeFactory.booleanNode(false);
                } else if (EMPTY_STRING.equals(value)) {
                    valueNode = this.nodeFactory.textNode("");
                } else {
                    valueNode = this.nodeFactory.textNode(value);
                }

                // root path is a value node, return this as the result for the whole row
                if (path.isEmpty()) {
                    return valueNode;
                }

                this.setObjectValue(object, path, valueNode);
            }
        }

        return this.convertObjectsToArrays(object, objectTypes, "");
    }

    /**
     * Set the value of a property inside an object using a path. Any objects on the path which do
     * not already exist will be created.
     *
     * @param object
     * @param path
     * @param value
     */
    private void setObjectValue(ObjectNode object, String path, JsonNode value) {
        String[] pathArray = path.split("/");
        for (int i = 0; i < pathArray.length; i++) {
            String propertyName = pathArray[i];

            if (i == pathArray.length - 1) {
                object.set(propertyName, value);
            } else {
                JsonNode child = object.get(propertyName);
                if (child == null) {
                    child = this.nodeFactory.objectNode();
                    object.set(propertyName, child);
                }

                if (!child.isContainerNode()) {
                    // child is a value node (most likely a null), can't set a property on a value node
                    // so just skip setting the value
                    return;
                }

                // can't be an array node (our tree is built only with objects)
                object = (ObjectNode) child;
            }
        }
    }

    /**
     * Traverses the object and replaces any child objects which contain all all integer field names
     * with array nodes.
     *
     * @param object
     * @param isArrayMap
     * @return
     */
    private JsonNode convertObjectsToArrays(ObjectNode object, Map<String, ObjectType> objectTypes, String path) {
        boolean hasChild = false;
        boolean allIntegers = true;
        int highestIndex = -1;

        // The type of object might have been explicitly set in the CSV, if not we will auto detect it
        ObjectType type = objectTypes.getOrDefault(path, ObjectType.NOT_SPECIFIED);

        Iterator<Entry<String, JsonNode>> it = object.fields();
        while(it.hasNext()) {
            hasChild = true;

            Entry<String, JsonNode> entry = it.next();
            String propertyName = entry.getKey();
            JsonNode value = entry.getValue();

            if (type == ObjectType.ARRAY || (type == ObjectType.NOT_SPECIFIED && allIntegers)) {
                if (INTEGER_PATTERN.matcher(propertyName).matches()) {
                    int index = Integer.parseInt(propertyName);
                    if (index > highestIndex) {
                        highestIndex = index;
                    }
                } else {
                    allIntegers = false;
                }
            }

            if (value.isObject()) {
                JsonNode result = this.convertObjectsToArrays((ObjectNode) value, objectTypes, path + "/" + propertyName);
                if (result.isArray()) {
                    entry.setValue(result);
                }
            }
        }

        // object type was not explicitly set and we detected an object will all integer keys,
        // assume its an array
        if (type == ObjectType.NOT_SPECIFIED && hasChild && allIntegers) {
            type = ObjectType.ARRAY;
        }

        if (type == ObjectType.ARRAY) {
            int size = highestIndex + 1;
            ArrayNode arrayNode = this.nodeFactory.arrayNode(size);
            for (int i = 0; i < size; i++) {
                JsonNode value = object.get(Integer.toString(i));
                if (value == null) {
                    value = this.nodeFactory.nullNode();
                }
                arrayNode.add(value);
            }

            return arrayNode;
        }

        return object;
    }
}
