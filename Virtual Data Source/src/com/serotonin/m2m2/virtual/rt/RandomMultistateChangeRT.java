/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.virtual.vo.RandomMultistateChangeVO;

public class RandomMultistateChangeRT extends ChangeTypeRT {
    private final RandomMultistateChangeVO vo;

    public RandomMultistateChangeRT(RandomMultistateChangeVO vo) {
        this.vo = vo;
    }

    @Override
    public DataValue change(DataValue currentValue) {
        int newValue = RANDOM.nextInt(vo.getValues().length);
        return new MultistateValue(vo.getValues()[newValue]);
    }
}
