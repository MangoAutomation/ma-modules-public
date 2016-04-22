/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.directwebremoting.annotations.DataTransferObject;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.util.IntMessagePair;
import com.serotonin.m2m2.virtual.rt.ChangeTypeRT;
import com.serotonin.util.SerializationHelper;

@DataTransferObject
abstract public class ChangeTypeVO implements Serializable, JsonSerializable {
    public interface Types {
        public static final int ALTERNATE_BOOLEAN = 1;
        public static final int BROWNIAN = 2;
        public static final int INCREMENT_ANALOG = 3;
        public static final int INCREMENT_MULTISTATE = 4;
        public static final int NO_CHANGE = 5;
        public static final int RANDOM_ANALOG = 6;
        public static final int RANDOM_BOOLEAN = 7;
        public static final int RANDOM_MULTISTATE = 8;
        public static final int ANALOG_ATTRACTOR = 9;
        public static final int SINUSOIDAL = 10;
    }

    public static final ExportCodes CHANGE_TYPE_CODES = new ExportCodes();
    static {
        CHANGE_TYPE_CODES.addElement(Types.ALTERNATE_BOOLEAN, "ALTERNATE_BOOLEAN",
                "dsEdit.virtual.changeType.alternate");
        CHANGE_TYPE_CODES.addElement(Types.BROWNIAN, "BROWNIAN", "dsEdit.virtual.changeType.brownian");
        CHANGE_TYPE_CODES.addElement(Types.INCREMENT_ANALOG, "INCREMENT_ANALOG", "dsEdit.virtual.changeType.increment");
        CHANGE_TYPE_CODES.addElement(Types.INCREMENT_MULTISTATE, "INCREMENT_MULTISTATE",
                "dsEdit.virtual.changeType.increment");
        CHANGE_TYPE_CODES.addElement(Types.NO_CHANGE, "NO_CHANGE", "dsEdit.virtual.changeType.noChange");
        CHANGE_TYPE_CODES.addElement(Types.RANDOM_ANALOG, "RANDOM_ANALOG", "dsEdit.virtual.changeType.random");
        CHANGE_TYPE_CODES.addElement(Types.RANDOM_BOOLEAN, "RANDOM_BOOLEAN", "dsEdit.virtual.changeType.random");
        CHANGE_TYPE_CODES.addElement(Types.RANDOM_MULTISTATE, "RANDOM_MULTISTATE", "dsEdit.virtual.changeType.random");
        CHANGE_TYPE_CODES.addElement(Types.ANALOG_ATTRACTOR, "ANALOG_ATTRACTOR", "dsEdit.virtual.changeType.attractor");
        CHANGE_TYPE_CODES.addElement(Types.SINUSOIDAL, "SINUSOIDAL", "dsEdit.virtual.changeType.sinusoidal");
    }

    public static IntMessagePair[] getChangeTypes(int dataTypeId) {
        switch (dataTypeId) {
        case DataTypes.BINARY:
            return new IntMessagePair[] { new IntMessagePair(Types.ALTERNATE_BOOLEAN, AlternateBooleanChangeVO.KEY),
                    new IntMessagePair(Types.NO_CHANGE, NoChangeVO.KEY),
                    new IntMessagePair(Types.RANDOM_BOOLEAN, RandomBooleanChangeVO.KEY), };
        case DataTypes.MULTISTATE:
            return new IntMessagePair[] {
                    new IntMessagePair(Types.INCREMENT_MULTISTATE, IncrementMultistateChangeVO.KEY),
                    new IntMessagePair(Types.NO_CHANGE, NoChangeVO.KEY),
                    new IntMessagePair(Types.RANDOM_MULTISTATE, RandomMultistateChangeVO.KEY), };
        case DataTypes.NUMERIC:
            return new IntMessagePair[] { new IntMessagePair(Types.BROWNIAN, BrownianChangeVO.KEY),
                    new IntMessagePair(Types.INCREMENT_ANALOG, IncrementAnalogChangeVO.KEY),
                    new IntMessagePair(Types.NO_CHANGE, NoChangeVO.KEY),
                    new IntMessagePair(Types.RANDOM_ANALOG, RandomAnalogChangeVO.KEY),
                    new IntMessagePair(Types.ANALOG_ATTRACTOR, AnalogAttractorChangeVO.KEY), 
            		new IntMessagePair(Types.SINUSOIDAL, SinusoidalChangeVO.KEY)};
            		
        case DataTypes.ALPHANUMERIC:
            return new IntMessagePair[] { new IntMessagePair(Types.NO_CHANGE, NoChangeVO.KEY), };
        }
        return new IntMessagePair[] {};
    }

    abstract public int typeId();

    abstract public TranslatableMessage getDescription();

    abstract public ChangeTypeRT createRuntime();

    @JsonProperty
    private String startValue;

    public String getStartValue() {
        return startValue;
    }

    public void setStartValue(String startValue) {
        this.startValue = startValue;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, startValue);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            startValue = SerializationHelper.readSafeUTF(in);
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("type", CHANGE_TYPE_CODES.getCode(typeId()));
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        // no op
    }
}
