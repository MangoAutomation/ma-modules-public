/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.io.IOException;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream;
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


    protected final INFO info;
    protected final Map<Integer, DataPointVO> voMap; //Point id to Vo
    
    public PointValueTimeQueryStream(INFO info, Map<Integer, DataPointVO> voMap) {
        this.info = info;
        this.voMap = voMap;
    }

    /*
     * (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream#getQueryInfo()
     */
    @Override
    public INFO getQueryInfo() {
        return info;
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream#getVoMap()
     */
    @Override
    public Map<Integer, DataPointVO> getVoMap() {
        return voMap;
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream#start()
     */
    @Override
    public void start(PointValueTimeWriter writer) throws IOException {
        if(info.isSingleArray())
            writer.writeStartArray();
        else
            writer.writeStartObject();
    }
    
    /* (non-Javadoc)
     * @see com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeStream#finish()
     */
    @Override
    public void finish(PointValueTimeWriter writer) throws IOException {
        if(info.isSingleArray())
            writer.writeEndArray();
        else
            writer.writeEndObject();
        
    }
}
