/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.pointValue.quantize;

import com.infiniteautomation.mango.quantize.BucketCalculator;
import com.infiniteautomation.mango.quantize.ValueChangeCounterQuantizer;
import com.infiniteautomation.mango.statistics.ValueChangeCounter;
import com.serotonin.m2m2.vo.DataPointVO;

/**
 *
 * @author Terry Packer
 */
public class ValueChangeCounterDataPointQuantizer extends DataPointStatisticsQuantizer<ValueChangeCounter> {

    public ValueChangeCounterDataPointQuantizer(DataPointVO vo, BucketCalculator calc, ChildStatisticsGeneratorCallback callback) {
        super(vo, callback);
        quantizer = new ValueChangeCounterQuantizer(calc, this);
    }

}
