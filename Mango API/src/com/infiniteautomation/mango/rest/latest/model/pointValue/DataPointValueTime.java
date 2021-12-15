/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue;

import java.io.IOException;

import com.goebl.simplify.Point;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * 
 * Interface to be able easily simplify and write values.
 * 
 * Comparable on the X value of the point.
 *
 * @author Terry Packer
 */
public interface DataPointValueTime extends Point {

    /**
     * return the data point for this value
     */
    public DataPointVO getVo();
    
    /**
     * Get the time of the value
     */
    public long getTime();
    
    /**
     */
    public abstract void writeEntry(PointValueTimeWriter writer, boolean useXid, boolean allowTimestamp) throws IOException;

    
}
