/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import com.serotonin.json.spi.JsonEntity;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.virtual.rt.ChangeTypeRT;
import com.serotonin.m2m2.virtual.rt.IncrementMultistateChangeRT;

@JsonEntity
public class IncrementMultistateChangeVO extends ChangeTypeVO {
    public static final TranslatableMessage KEY = new TranslatableMessage("dsEdit.virtual.changeType.increment");

    private int[] values = new int[0];
    private boolean roll;

    @Override
    public int typeId() {
        return Types.INCREMENT_MULTISTATE;
    }

    @Override
    public TranslatableMessage getDescription() {
        return KEY;
    }

    @Override
    public ChangeTypeRT createRuntime() {
        return new IncrementMultistateChangeRT(this);
    }

    public boolean isRoll() {
        return roll;
    }

    public void setRoll(boolean roll) {
        this.roll = roll;
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

    @Override
    public void addProperties(List<TranslatableMessage> list) {
        super.addProperties(list);
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.values", Arrays.toString(values));
        AuditEventType.addPropertyMessage(list, "dsEdit.virtual.roll", roll);
    }

    @Override
    public void addPropertyChanges(List<TranslatableMessage> list, Object o) {
        super.addPropertyChanges(list, o);
        IncrementMultistateChangeVO from = (IncrementMultistateChangeVO) o;
        if (Arrays.equals(from.values, values))
            AuditEventType.addPropertyChangeMessage(list, "dsEdit.virtual.values", Arrays.toString(from.values),
                    Arrays.toString(values));
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
        out.writeObject(values);
        out.writeBoolean(roll);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            values = (int[]) in.readObject();
            roll = in.readBoolean();
        }
    }
}
