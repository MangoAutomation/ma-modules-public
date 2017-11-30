/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.v2.model.pointValue.quantize;

import java.io.IOException;

/**
 *
 * @author Terry Packer
 */
public interface ChildStatisticsGeneratorCallback {

    /**
     * 
     * Called upon period change
     * @param generator
     */
    void quantizedStatistics(DataPointStatisticsGenerator generator) throws IOException;
    
    /**
     * Called if the query was cancelled
     * @param e
     */
    void cancelled(IOException e);
}
