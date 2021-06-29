/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.virtual.vo.BrownianChangeVO;

public class BrownianChangeRT extends ChangeTypeRT {
    private final BrownianChangeVO vo;

    public BrownianChangeRT(BrownianChangeVO vo) {
        this.vo = vo;
    }

    @Override
    public DataValue change(DataValue currentValue) {
        double change = RANDOM.nextDouble() * vo.getMaxChange() * 2 - vo.getMaxChange();
        double newValue = currentValue.getDoubleValue() + change;
        if (newValue > vo.getMax())
            newValue = vo.getMax();
        if (newValue < vo.getMin())
            newValue = vo.getMin();
        return new NumericValue(newValue);
    }
}
