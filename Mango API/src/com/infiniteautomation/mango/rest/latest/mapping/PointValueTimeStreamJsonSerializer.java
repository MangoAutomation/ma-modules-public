/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.mapping;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeJsonWriter;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeStream;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeStream.StreamContentType;
import com.infiniteautomation.mango.rest.latest.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.latest.model.pointValue.query.LatestQueryInfo;

/**
 *
 * @author Terry Packer
 */
public class PointValueTimeStreamJsonSerializer<T, INFO extends LatestQueryInfo> extends JsonSerializer<PointValueTimeStream<T, INFO>> {
    private static final Logger LOG = LoggerFactory.getLogger(PointValueTimeStreamJsonSerializer.class);

    @Override
    public void serialize(PointValueTimeStream<T, INFO> value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
    JsonProcessingException {
        PointValueTimeWriter writer = new PointValueTimeJsonWriter(value.getQueryInfo(), jgen);
        value.setContentType(StreamContentType.JSON);
        try{
            value.start(writer);
            value.streamData(writer);
            value.finish(writer);
            jgen.flush();
        }catch(QueryCancelledException e) {
            LOG.info("Query cancelled.", e);
        }
    }

}
