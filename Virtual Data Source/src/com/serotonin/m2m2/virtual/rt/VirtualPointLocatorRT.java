/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;

public class VirtualPointLocatorRT extends PointLocatorRT {
    private final ChangeTypeRT changeType;
    private DataValue currentValue;
    private final boolean settable;

    public VirtualPointLocatorRT(ChangeTypeRT changeType, DataValue startValue, boolean settable) {
        this.changeType = changeType;
        currentValue = startValue;
        this.settable = settable;
    }

    public ChangeTypeRT getChangeType() {
        return changeType;
    }

    public DataValue getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(DataValue currentValue) {
        this.currentValue = currentValue;
    }

    public void change() {
        currentValue = changeType.change(currentValue);
    }

    @Override
    public boolean isSettable() {
        return settable;
    }
}
