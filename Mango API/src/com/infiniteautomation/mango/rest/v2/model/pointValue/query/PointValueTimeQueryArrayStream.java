/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.query;

import java.util.Map;

import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.mvc.rest.v1.model.QueryArrayStream;

/**
 * 
 * Base class for all PointValueTime Query Streams
 * 
 * Could be from database or an in memory list and the output
 * could be one point value, lists, or lists of lists
 *
 * @author Terry Packer
 */
public abstract class PointValueTimeQueryArrayStream<T> implements QueryArrayStream<T>{

    public enum StreamType {
        JSON,
        CSV
    }
    
    protected final ZonedDateTimeRangeQueryInfo info;
    protected final Map<Integer, DataPointVO> voMap; //Point id to Vo
    protected StreamType  streamType;
    protected PointValueTimeWriter writer;
    
    public PointValueTimeQueryArrayStream(ZonedDateTimeRangeQueryInfo info, Map<Integer, DataPointVO> voMap) {
        this.info = info;
        this.voMap = voMap;
    }

}
