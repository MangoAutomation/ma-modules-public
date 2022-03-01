/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.genericcsv;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingMultiPointModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.ValueTimeModel;

/**
 * @author Jared Wiltshire
 */
public class CsvJacksonModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public CsvJacksonModule() {
        super("CsvJacksonModule");
        LongAsDateSerializer longAsDateSerializer = new LongAsDateSerializer();
        addSerializer(Long.class, longAsDateSerializer);
        addSerializer(long.class, longAsDateSerializer);

        LongAsDateDeserializer longAsDateDeserializer = new LongAsDateDeserializer();
        addDeserializer(Long.class, longAsDateDeserializer);
        addDeserializer(long.class, longAsDateDeserializer);

        addSerializer(StreamingMultiPointModel.class, new StreamingMultiPointModelSerializer());
        addSerializer(StreamingPointValueTimeModel.class, new StreamingPointValueTimeModelSerializer());
    }

    public static class StreamingMultiPointModelSerializer extends StdSerializer<StreamingMultiPointModel> {

        protected StreamingMultiPointModelSerializer() {
            super(StreamingMultiPointModel.class);
        }

        @Override
        public void serialize(StreamingMultiPointModel value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            var timestamp = value.getTimestamp();
            if (timestamp instanceof Long) {
                gen.writeNumberField(PointValueField.TIMESTAMP.getFieldName(), (Long) timestamp);
            } else if (timestamp instanceof String) {
                gen.writeStringField(PointValueField.TIMESTAMP.getFieldName(), (String) timestamp);
            }

            for (var entry : value.getPointValues().entrySet()) {
                var serializer = new StreamingPointValueTimeModelSerializer(entry.getKey());
                serializer.serialize(entry.getValue(), gen, provider);
            }

            gen.writeEndObject();
        }
    }

    public static class StreamingPointValueTimeModelSerializer extends StdSerializer<StreamingPointValueTimeModel> {

        private final boolean unwrapped;
        private final NameTransformer nameTransformer;

        protected StreamingPointValueTimeModelSerializer() {
            super(StreamingPointValueTimeModel.class);
            this.unwrapped = false;
            this.nameTransformer = NameTransformer.NOP;
        }

        protected StreamingPointValueTimeModelSerializer(String xid) {
            this(new XidPrefixNameTransformer(xid));
        }

        protected StreamingPointValueTimeModelSerializer(NameTransformer nameTransformer) {
            super(StreamingPointValueTimeModel.class);
            this.unwrapped = true;
            this.nameTransformer = nameTransformer;
        }

        @Override
        public void serialize(StreamingPointValueTimeModel value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (!unwrapped) {
                gen.writeStartObject();
            }

            var serializer = provider.findValueSerializer(ValueTimeModel.class)
                    .unwrappingSerializer(nameTransformer);
            serializer.serialize(value.getValueModel(), gen, provider);

            provider.defaultSerializeField(fieldName(PointValueField.ANNOTATION), value.getAnnotation(), gen);
            provider.defaultSerializeField(fieldName(PointValueField.CACHED), value.getCached(), gen);
            provider.defaultSerializeField(fieldName(PointValueField.BOOKEND), value.getBookend(), gen);
            provider.defaultSerializeField(fieldName(PointValueField.XID), value.getXid(), gen);
            provider.defaultSerializeField(fieldName(PointValueField.NAME), value.getName(), gen);
            provider.defaultSerializeField(fieldName(PointValueField.DEVICE_NAME), value.getDeviceName(), gen);
            provider.defaultSerializeField(fieldName(PointValueField.DATA_SOURCE_NAME), value.getDataSourceName(), gen);

            if (!unwrapped) {
                gen.writeEndObject();
            }
        }

        @Override
        public boolean isUnwrappingSerializer() {
            return unwrapped;
        }

        @Override
        public JsonSerializer<StreamingPointValueTimeModel> unwrappingSerializer(NameTransformer unwrapper) {
            return new StreamingPointValueTimeModelSerializer(unwrapper);
        }

        private String fieldName(PointValueField field) {
            return nameTransformer.transform(field.getFieldName());
        }
    }

    public static class XidPrefixNameTransformer extends NameTransformer {
        private final String xid;

        public XidPrefixNameTransformer(String xid) {
            this.xid = xid;
        }

        @Override
        public String transform(String name) {
            if (name.equals(PointValueField.VALUE.getFieldName())) {
                return xid;
            }
            return xid + "." + name;
        }

        @Override
        public String reverse(String transformed) {
            if (transformed.equals(xid)) {
                return PointValueField.VALUE.getFieldName();
            } else if (transformed.startsWith(xid + ".")) {
                return transformed.substring((xid + ".").length());
            }
            return null;
        }
    }

}