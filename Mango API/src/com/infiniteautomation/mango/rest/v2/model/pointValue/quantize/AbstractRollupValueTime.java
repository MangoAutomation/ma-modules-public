/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;

import com.goebl.simplify.Point;
import com.infiniteautomation.mango.rest.v2.model.pointValue.PointValueTimeWriter;

/**
 * 
 * 
 * 
 * @author Terry Packer
 */
public abstract class AbstractRollupValueTime implements Point, Comparable<AbstractRollupValueTime>{

    /**
     * @param writer
     */
    public abstract void writeValue(PointValueTimeWriter writer) throws IOException;
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(AbstractRollupValueTime that) {
        if (getX() < that.getX())
            return -1;
        if (getX() > that.getX())
            return 1;
        return 0;
    }
    
}
