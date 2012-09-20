/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.virtual.vo.IncrementAnalogChangeVO;

public class IncrementAnalogChangeRT extends ChangeTypeRT {
    private final IncrementAnalogChangeVO vo;
    private boolean decrement = false;

    public IncrementAnalogChangeRT(IncrementAnalogChangeVO vo) {
        this.vo = vo;
    }

    @Override
    public DataValue change(DataValue currentValue) {
        double newValue = currentValue.getDoubleValue();

        if (vo.isRoll()) {
            newValue += vo.getChange();
            if (newValue > vo.getMax())
                newValue = vo.getMin();
            if (newValue < vo.getMin())
                newValue = vo.getMax();
        }
        else {
            if (decrement) {
                newValue -= vo.getChange();
                if (newValue <= vo.getMin()) {
                    newValue = vo.getMin();
                    decrement = false;
                }
            }
            else {
                newValue += vo.getChange();
                if (newValue >= vo.getMax()) {
                    newValue = vo.getMax();
                    decrement = true;
                }
            }
        }

        return new NumericValue(newValue);
    }
}
