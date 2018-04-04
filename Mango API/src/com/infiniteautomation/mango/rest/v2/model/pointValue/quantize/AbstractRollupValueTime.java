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
public abstract class AbstractRollupValueTime implements Point{

    /**
     * @param writer
     */
    public abstract void writePointValueTime(PointValueTimeWriter writer) throws IOException;
    
    
}
