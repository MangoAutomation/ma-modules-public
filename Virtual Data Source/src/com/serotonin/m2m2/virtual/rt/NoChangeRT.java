/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.rt;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;

public class NoChangeRT extends ChangeTypeRT {
    @Override
    public DataValue change(DataValue currentValue) {
        return currentValue;
    }
}
