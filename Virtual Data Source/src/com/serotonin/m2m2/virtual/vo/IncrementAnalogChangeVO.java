/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.virtual.rt.ChangeTypeRT;
import com.serotonin.m2m2.virtual.rt.IncrementAnalogChangeRT;

public class IncrementAnalogChangeVO extends ChangeTypeVO {
    public static final TranslatableMessage KEY = new TranslatableMessage("dsEdit.virtual.changeType.increment");

    @JsonProperty
    private double min;
    @JsonProperty
    private double max;
    @JsonProperty
    private double change;
    @JsonProperty
    private boolean roll;

    @Override
    public int typeId() {
        return Types.INCREMENT_ANALOG;
    }

    @Override
    public TranslatableMessage getDescription() {
        return KEY;
    }

    @Override
    public ChangeTypeRT createRuntime() {
        return new IncrementAnalogChangeRT(this);
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
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

    public boolean isRoll() {
        return roll;
    }

    public void setRoll(boolean roll) {
        this.roll = roll;
    }

    @Override
    public void addProperties(List<TranslatableMessage> list) {
        super.addProperties(list);
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.min", min);
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.max", max);
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.change", change);
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.roll", roll);
    }

    @Override
    public void addPropertyChanges(List<TranslatableMessage> list, Object o) {
        super.addPropertyChanges(list, o);
        IncrementAnalogChangeVO from = (IncrementAnalogChangeVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.virtual.min", from.min, min);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.virtual.max", from.max, max);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.virtual.change", from.change, change);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.virtual.roll", from.roll, roll);
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeDouble(min);
        out.writeDouble(max);
        out.writeDouble(change);
        out.writeBoolean(roll);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            min = in.readDouble();
            max = in.readDouble();
            change = in.readDouble();
            roll = in.readBoolean();
        }
    }
}
