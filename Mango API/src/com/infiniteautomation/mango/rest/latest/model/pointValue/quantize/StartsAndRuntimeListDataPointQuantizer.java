/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.quantize;

import com.infiniteautomation.mango.quantize.BucketCalculator;
import com.infiniteautomation.mango.quantize.StartsAndRuntimeListQuantizer;
import com.infiniteautomation.mango.statistics.StartsAndRuntimeList;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class StartsAndRuntimeListDataPointQuantizer extends DataPointStatisticsQuantizer<StartsAndRuntimeList> {

    public StartsAndRuntimeListDataPointQuantizer(DataPointVO vo, BucketCalculator calc, ChildStatisticsGeneratorCallback callback) {
        super(vo, callback);
        quantizer = new StartsAndRuntimeListQuantizer(calc, this);
    }

}
