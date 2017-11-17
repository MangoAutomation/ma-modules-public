/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.serotonin.m2m2.web.mvc.rest.v1.csv.CSVPojoWriter;

/**
 *
 * @author Terry Packer
 */
public interface PointValueTimeStream<T> {
    
    public boolean isSingleArray();
    
    public void streamData(JsonGenerator jgen) throws IOException;
    
    public void streamData(CSVPojoWriter<T> writer) throws IOException;
}
