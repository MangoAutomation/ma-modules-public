/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.genericcsv;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueField;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingMultiPointModel;
import com.infiniteautomation.mango.rest.latest.streamingvalues.model.StreamingPointValueTimeModel;

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

            var baseSerializer = provider.findValueSerializer(StreamingPointValueTimeModel.class);
            for (var entry : value.getPointValues().entrySet()) {
                var unwrappingSerializer = baseSerializer
                        .unwrappingSerializer(new XidPrefixNameTransformer(entry.getKey()));
                unwrappingSerializer.serialize(entry.getValue(), gen, provider);
            }

            gen.writeEndObject();
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