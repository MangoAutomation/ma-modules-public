/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.mapping;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeJsonWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream.StreamContentType;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;

/**
 *
 * @author Terry Packer
 */
public class PointValueTimeStreamJsonSerializer<T, INFO extends LatestQueryInfo> extends JsonSerializer<PointValueTimeStream<T, INFO>> {
    private static final Log LOG = LogFactory.getLog(PointValueTimeStreamJsonSerializer.class);

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
