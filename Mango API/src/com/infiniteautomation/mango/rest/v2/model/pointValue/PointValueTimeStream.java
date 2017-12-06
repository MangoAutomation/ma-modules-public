/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;
import java.util.Map;

import com.infiniteautomation.mango.rest.v2.model.pointValue.query.LatestQueryInfo;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public interface PointValueTimeStream<T, INFO extends LatestQueryInfo> {
    
    public enum StreamContentType {
        JSON,
        CSV
    }
    
    public INFO getQueryInfo();
    public Map<Integer, DataPointVO> getVoMap();
    
    public void start(PointValueTimeWriter writer) throws IOException ;
    public void streamData(PointValueTimeWriter writer) throws IOException;
    public void finish(PointValueTimeWriter writer) throws IOException ;
    
    /**
     * Set the content type of the stream CSV or JSON
     * @param type
     */
    public void setContentType(StreamContentType type);
    
}
