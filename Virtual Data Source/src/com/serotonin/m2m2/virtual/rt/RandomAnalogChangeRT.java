/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.virtual.vo.RandomAnalogChangeVO;

public class RandomAnalogChangeRT extends ChangeTypeRT {
    private final RandomAnalogChangeVO vo;

    public RandomAnalogChangeRT(RandomAnalogChangeVO vo) {
        this.vo = vo;
    }

    @Override
    public DataValue change(DataValue currentValue) {
        double newValue = RANDOM.nextDouble();
        return new NumericValue((vo.getMax() - vo.getMin()) * newValue + vo.getMin());
    }
}
