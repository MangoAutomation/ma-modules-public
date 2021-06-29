/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;

public class RandomBooleanChangeRT extends ChangeTypeRT {
    @Override
    public DataValue change(DataValue currentValue) {
        return new BinaryValue(RANDOM.nextBoolean());
    }
}
