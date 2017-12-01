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
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeJsonWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream.StreamContentType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;

/**
 *
 * @author Terry Packer
 */
public class PointValueTimeStreamJsonSerializer<T, INFO extends LatestQueryInfo> extends JsonSerializer<PointValueTimeStream<T, INFO>>{

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(PointValueTimeStream<T, INFO> value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        PointValueTimeWriter writer = new PointValueTimeJsonWriter(value.getQueryInfo(), jgen);
        value.setContentType(StreamContentType.JSON);
        value.start(writer);
        value.streamData(writer);
        value.finish(writer);
        if(value.cancelled())
            throw value.getError();
        jgen.flush();
    }

}
