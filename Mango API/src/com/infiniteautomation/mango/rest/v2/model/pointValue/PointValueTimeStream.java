/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.infiniteautomation.mango.rest.v2.model.pointValue.query.ZonedDateTimeRangeQueryInfo;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public interface PointValueTimeStream<T> {
    
    public ZonedDateTimeRangeQueryInfo getQueryInfo();
    public Map<Integer, DataPointVO> getVoMap();
    
    public void start() throws IOException ;
    public void streamData(JsonGenerator jgen) throws IOException;
    public void finish() throws IOException ;
}
