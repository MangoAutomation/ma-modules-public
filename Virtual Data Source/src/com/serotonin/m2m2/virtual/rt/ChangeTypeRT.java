/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.virtual.rt;

import java.util.Random;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;

abstract public class ChangeTypeRT {
    protected static final Random RANDOM = new Random();

    abstract public DataValue change(DataValue currentValue);
}
