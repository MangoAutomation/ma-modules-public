/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.quantize;

import com.infiniteautomation.mango.quantize.BucketCalculator;
import com.infiniteautomation.mango.quantize.NoStatisticsQuantizer;
import com.infiniteautomation.mango.statistics.NoStatisticsGenerator;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 * 
 *
 * @author Terry Packer
 */
public class NoStatisticsDataPointQuantizer extends DataPointStatisticsQuantizer<NoStatisticsGenerator>{

    /**
     */
    public NoStatisticsDataPointQuantizer(DataPointVO vo,
            BucketCalculator calc,
            ChildStatisticsGeneratorCallback callback) {
        super(vo, callback);
        quantizer = new NoStatisticsQuantizer(calc, this);
    }

}
