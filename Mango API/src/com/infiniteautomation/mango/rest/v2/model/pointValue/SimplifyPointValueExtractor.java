/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue;

import com.goebl.simplify.PointExtractor;

/**
 *
 * @author Terry Packer
 */
public class SimplifyPointValueExtractor implements PointExtractor<DataPointVOPointValueTimeBookend>{
    
    public static SimplifyPointValueExtractor extractor = new SimplifyPointValueExtractor();
    
    @Override
    public double getX(DataPointVOPointValueTimeBookend point) {
        return point.getPvt().getDoubleValue();
    }

    @Override
    public double getY(DataPointVOPointValueTimeBookend point) {
        return point.getTime(); 
    }
}
