/**
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
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
     * @param vo
     * @param callback
     */
    public NoStatisticsDataPointQuantizer(DataPointVO vo,
            BucketCalculator calc,
            ChildStatisticsGeneratorCallback callback) {
        super(vo, callback);
        quantizer = new NoStatisticsQuantizer(calc, this);
    }

}
