/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.goebl.simplify;

import java.io.IOException;

import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * 
 * Interface to be able easily simplify and write values.
 * 
 * Comparable on the X value of the point.
 *
 * @author Terry Packer
 */
public interface SimplifiableValue extends Point {

    /**
     * return the data point for this value
     * @return
     */
    public DataPointVO getVo();
    
    /**
     * Get the time of the value
     * @return
     */
    public long getTime();
    
    /**
     * @param writer
     */
    public abstract void writeEntry(PointValueTimeWriter writer, boolean useXid, boolean allowTimestamp) throws IOException;

    
}
