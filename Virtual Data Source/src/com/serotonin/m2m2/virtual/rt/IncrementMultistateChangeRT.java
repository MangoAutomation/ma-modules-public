/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.virtual.vo.IncrementMultistateChangeVO;

public class IncrementMultistateChangeRT extends ChangeTypeRT {
    private final IncrementMultistateChangeVO vo;
    private boolean decrement;

    public IncrementMultistateChangeRT(IncrementMultistateChangeVO vo) {
        this.vo = vo;
    }

    @Override
    public DataValue change(DataValue currentValue) {
        // Get the current index.
        int currentInt = currentValue.getIntegerValue();
        int index = -1;
        for (int i = 0; i < vo.getValues().length; i++) {
            if (vo.getValues()[i] == currentInt) {
                index = i;
                break;
            }
        }

        if (index == -1)
            return new MultistateValue(vo.getValues()[0]);

        if (vo.isRoll()) {
            index++;
            if (index >= vo.getValues().length)
                index = 0;
        }
        else {
            if (decrement) {
                index--;
                if (index == -1) {
                    index = 1;
                    decrement = false;
                }
            }
            else {
                index++;
                if (index == vo.getValues().length) {
                    index = vo.getValues().length - 2;
                    decrement = true;
                }
            }
        }

        return new MultistateValue(vo.getValues()[index]);
    }
}
