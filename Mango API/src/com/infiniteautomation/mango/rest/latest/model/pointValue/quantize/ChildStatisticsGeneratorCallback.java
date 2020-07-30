/**
 * @copyright 2017 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.quantize;

import com.infiniteautomation.mango.db.query.QueryCancelledException;

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
    void quantizedStatistics(DataPointStatisticsGenerator generator) throws QueryCancelledException;

}
