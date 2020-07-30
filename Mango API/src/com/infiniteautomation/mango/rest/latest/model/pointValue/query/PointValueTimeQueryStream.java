/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.Map;

import com.infiniteautomation.mango.db.query.QueryCancelledException;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * Base class for all PointValueTime Query Streams
 *
 * Could be from database or an in memory list and the output
 * could be one point value, lists, or lists of lists
 *
 * @author Terry Packer
 */
public abstract class PointValueTimeQueryStream<T, INFO extends LatestQueryInfo> implements PointValueTimeStream<T, INFO>{

    protected StreamContentType contentType;
    protected final INFO info;
    protected final Map<Integer, DataPointVO> voMap; //Point id to Vo
    protected IOException error;

    public PointValueTimeQueryStream(INFO info, Map<Integer, DataPointVO> voMap) {
        this.info = info;
        this.voMap = voMap;
    }

    @Override
    public INFO getQueryInfo() {
        return info;
    }

    @Override
    public Map<Integer, DataPointVO> getVoMap() {
        return voMap;
    }

    @Override
    public void start(PointValueTimeWriter writer) throws QueryCancelledException, IOException {
        if(info.isSingleArray())
            writer.writeStartArray();
        else {
            if(contentType == StreamContentType.JSON)
                writer.writeStartObject();
        }
    }

    @Override
    public void finish(PointValueTimeWriter writer) throws QueryCancelledException, IOException {
        if(info.isSingleArray())
            writer.writeEndArray();
        else {
            if(contentType == StreamContentType.JSON)
                writer.writeEndObject();
        }
    }

    @Override
    public void setContentType(StreamContentType type) {
        this.contentType = type;
    }
}
