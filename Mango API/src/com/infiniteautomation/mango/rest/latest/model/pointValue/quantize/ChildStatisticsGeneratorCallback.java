/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
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
     */
    void quantizedStatistics(DataPointStatisticsGenerator generator) throws QueryCancelledException;

}
