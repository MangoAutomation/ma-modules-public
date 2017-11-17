/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;

/**
 *
 * @author Terry Packer
 */
public class PointValueTimeStreamSerializer<T> extends JsonSerializer<PointValueTimeStream<T>>{

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(PointValueTimeStream<T> value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        
        if(value.isSingleArray()) {
            jgen.writeStartArray();
            value.streamData(jgen);
            jgen.writeEndArray();
        }else {
            jgen.writeStartObject();
            value.streamData(jgen);
            jgen.writeEndObject();
        }
    }

}
