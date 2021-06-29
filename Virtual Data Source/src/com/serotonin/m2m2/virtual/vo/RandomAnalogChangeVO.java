/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.virtual.rt.ChangeTypeRT;
import com.serotonin.m2m2.virtual.rt.RandomAnalogChangeRT;

public class RandomAnalogChangeVO extends ChangeTypeVO {
    public static final TranslatableMessage KEY = new TranslatableMessage("dsEdit.virtual.changeType.random");

    @JsonProperty
    private double min;
    @JsonProperty
    private double max;

    @Override
    public int typeId() {
        return Types.RANDOM_ANALOG;
    }

    @Override
    public TranslatableMessage getDescription() {
        return KEY;
    }

    @Override
    public ChangeTypeRT createRuntime() {
        return new RandomAnalogChangeRT(this);
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeDouble(min);
        out.writeDouble(max);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            min = in.readDouble();
            max = in.readDouble();
        }
    }
}
