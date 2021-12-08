/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;
import com.serotonin.m2m2.virtual.VirtualDataSourceDefinition;
import com.serotonin.m2m2.virtual.rt.ChangeTypeRT;
import com.serotonin.m2m2.virtual.rt.VirtualPointLocatorRT;
import com.serotonin.m2m2.virtual.vo.ChangeTypeVO.Types;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;

public class VirtualPointLocatorVO extends AbstractPointLocatorVO<VirtualPointLocatorVO> implements
JsonSerializable {
    private static final Logger LOG = LoggerFactory.getLogger(VirtualPointLocatorVO.class);

    @Override
    public TranslatableMessage getConfigurationDescription() {
        ChangeTypeVO changeType = getChangeType();
        if (changeType == null)
            throw new ShouldNeverHappenException("unknown change type");
        return changeType.getDescription();
    }

    public ChangeTypeVO getChangeType() {
        if (changeTypeId == Types.ALTERNATE_BOOLEAN)
            return alternateBooleanChange;
        if (changeTypeId == Types.BROWNIAN)
            return brownianChange;
        if (changeTypeId == Types.INCREMENT_ANALOG)
            return incrementAnalogChange;
        if (changeTypeId == Types.INCREMENT_MULTISTATE)
            return incrementMultistateChange;
        if (changeTypeId == Types.NO_CHANGE)
            return noChange;
        if (changeTypeId == Types.RANDOM_ANALOG)
            return randomAnalogChange;
        if (changeTypeId == Types.RANDOM_BOOLEAN)
            return randomBooleanChange;
        if (changeTypeId == Types.RANDOM_MULTISTATE)
            return randomMultistateChange;
        if (changeTypeId == Types.ANALOG_ATTRACTOR)
            return analogAttractorChange;
        if (changeTypeId == Types.SINUSOIDAL)
            return sinusoidalChange;
        LOG.error("Failed to resolve changeTypeId " + changeTypeId
                + " for virtual point locator");
        return alternateBooleanChange;
    }

    @Override
    public PointLocatorRT<VirtualPointLocatorVO> createRuntime() {
        ChangeTypeRT changeType = getChangeType().createRuntime();
        String startValue = getChangeType().getStartValue();
        DataValue startObject;
        if (dataType == DataTypes.BINARY)
            startObject = BinaryValue.parseBinary(startValue);
        else if (dataType == DataTypes.MULTISTATE) {
            try {
                startObject = MultistateValue.parseMultistate(startValue);
            } catch (NumberFormatException e) {
                startObject = new MultistateValue(0);
            }
        } else if (dataType == DataTypes.NUMERIC) {
            try {
                startObject = NumericValue.parseNumeric(startValue);
            } catch (NumberFormatException e) {
                startObject = new NumericValue(0);
            }
        } else {
            if (startValue == null)
                startObject = new AlphanumericValue("");
            else
                startObject = new AlphanumericValue(startValue);
        }
        return new VirtualPointLocatorRT(this, changeType, startObject, isSettable());
    }

    @Override
    public String getDataSourceType() {
        return VirtualDataSourceDefinition.TYPE_NAME;
    }

    private DataTypes dataType = DataTypes.BINARY;
    private int changeTypeId = Types.ALTERNATE_BOOLEAN;
    @JsonProperty
    private boolean settable;
    private AlternateBooleanChangeVO alternateBooleanChange = new AlternateBooleanChangeVO();
    private BrownianChangeVO brownianChange = new BrownianChangeVO();
    private IncrementAnalogChangeVO incrementAnalogChange = new IncrementAnalogChangeVO();
    private IncrementMultistateChangeVO incrementMultistateChange = new IncrementMultistateChangeVO();
    private NoChangeVO noChange = new NoChangeVO();
    private RandomAnalogChangeVO randomAnalogChange = new RandomAnalogChangeVO();
    private RandomBooleanChangeVO randomBooleanChange = new RandomBooleanChangeVO();
    private RandomMultistateChangeVO randomMultistateChange = new RandomMultistateChangeVO();
    private AnalogAttractorChangeVO analogAttractorChange = new AnalogAttractorChangeVO();
    private SinusoidalChangeVO sinusoidalChange = new SinusoidalChangeVO();

    public int getChangeTypeId() {
        return changeTypeId;
    }

    public void setChangeTypeId(int changeTypeId) {
        this.changeTypeId = changeTypeId;
    }

    @Override
    public DataTypes getDataType() {
        return dataType;
    }

    public void setDataType(DataTypes dataType) {
        this.dataType = dataType;
    }

    public AlternateBooleanChangeVO getAlternateBooleanChange() {
        return alternateBooleanChange;
    }

    public BrownianChangeVO getBrownianChange() {
        return brownianChange;
    }

    public IncrementAnalogChangeVO getIncrementAnalogChange() {
        return incrementAnalogChange;
    }

    public IncrementMultistateChangeVO getIncrementMultistateChange() {
        return incrementMultistateChange;
    }

    public NoChangeVO getNoChange() {
        return noChange;
    }

    public RandomAnalogChangeVO getRandomAnalogChange() {
        return randomAnalogChange;
    }

    public RandomBooleanChangeVO getRandomBooleanChange() {
        return randomBooleanChange;
    }

    public RandomMultistateChangeVO getRandomMultistateChange() {
        return randomMultistateChange;
    }

    public SinusoidalChangeVO getSinusoidalChange() {
        return sinusoidalChange;
    }

    @Override
    public boolean isSettable() {
        return settable;
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    public AnalogAttractorChangeVO getAnalogAttractorChange() {
        return analogAttractorChange;
    }

    public void setAlternateBooleanChange(
            AlternateBooleanChangeVO alternateBooleanChange) {
        this.alternateBooleanChange = alternateBooleanChange;
    }

    public void setBrownianChange(BrownianChangeVO brownianChange) {
        this.brownianChange = brownianChange;
    }

    public void setIncrementAnalogChange(
            IncrementAnalogChangeVO incrementAnalogChange) {
        this.incrementAnalogChange = incrementAnalogChange;
    }

    public void setIncrementMultistateChange(
            IncrementMultistateChangeVO incrementMultistateChange) {
        this.incrementMultistateChange = incrementMultistateChange;
    }

    public void setNoChange(NoChangeVO noChange) {
        this.noChange = noChange;
    }

    public void setRandomAnalogChange(RandomAnalogChangeVO randomAnalogChange) {
        this.randomAnalogChange = randomAnalogChange;
    }

    public void setRandomBooleanChange(RandomBooleanChangeVO randomBooleanChange) {
        this.randomBooleanChange = randomBooleanChange;
    }

    public void setRandomMultistateChange(
            RandomMultistateChangeVO randomMultistateChange) {
        this.randomMultistateChange = randomMultistateChange;
    }

    public void setAnalogAttractorChange(
            AnalogAttractorChangeVO analogAttractorChange) {
        this.analogAttractorChange = analogAttractorChange;
    }

    public void setSinusoidalChange(SinusoidalChangeVO sinusoidal) {
        this.sinusoidalChange = sinusoidal;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(dataType.getId());
        out.writeInt(changeTypeId);
        out.writeBoolean(settable);
        out.writeObject(alternateBooleanChange);
        out.writeObject(brownianChange);
        out.writeObject(incrementAnalogChange);
        out.writeObject(incrementMultistateChange);
        out.writeObject(noChange);
        out.writeObject(randomAnalogChange);
        out.writeObject(randomBooleanChange);
        out.writeObject(randomMultistateChange);
        out.writeObject(analogAttractorChange);
        out.writeObject(sinusoidalChange);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            dataType = DataTypes.fromId(in.readInt());
            changeTypeId = in.readInt();
            settable = in.readBoolean();
            alternateBooleanChange = (AlternateBooleanChangeVO) in.readObject();
            brownianChange = (BrownianChangeVO) in.readObject();
            incrementAnalogChange = (IncrementAnalogChangeVO) in.readObject();
            incrementMultistateChange = (IncrementMultistateChangeVO) in.readObject();
            noChange = (NoChangeVO) in.readObject();
            randomAnalogChange = (RandomAnalogChangeVO) in.readObject();
            randomBooleanChange = (RandomBooleanChangeVO) in.readObject();
            randomMultistateChange = (RandomMultistateChangeVO) in.readObject();
            analogAttractorChange = (AnalogAttractorChangeVO) in.readObject();
            sinusoidalChange = new SinusoidalChangeVO();
        }
        if(ver == 2){
            dataType = DataTypes.fromId(in.readInt());
            changeTypeId = in.readInt();
            settable = in.readBoolean();
            alternateBooleanChange = (AlternateBooleanChangeVO) in.readObject();
            brownianChange = (BrownianChangeVO) in.readObject();
            incrementAnalogChange = (IncrementAnalogChangeVO) in.readObject();
            incrementMultistateChange = (IncrementMultistateChangeVO) in.readObject();
            noChange = (NoChangeVO) in.readObject();
            randomAnalogChange = (RandomAnalogChangeVO) in.readObject();
            randomBooleanChange = (RandomBooleanChangeVO) in.readObject();
            randomMultistateChange = (RandomMultistateChangeVO) in.readObject();
            analogAttractorChange = (AnalogAttractorChangeVO) in.readObject();
            sinusoidalChange = (SinusoidalChangeVO) in.readObject();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException,
    JsonException {
        writeDataType(writer);
        writer.writeEntry("changeType", getChangeType());

    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject)
            throws JsonException {
        if (jsonObject.containsKey("dataType")) {
            this.dataType = readDataType(jsonObject, DataTypes.IMAGE);
        }

        JsonObject ctjson = jsonObject.getJsonObject("changeType");
        if (ctjson == null)
            throw new TranslatableJsonException("emport.error.missingObject",
                    "changeType");

        String text = ctjson.getString("type");
        if (text == null)
            throw new TranslatableJsonException("emport.error.missing", "type",
                    ChangeTypeVO.CHANGE_TYPE_CODES.getCodeList());

        changeTypeId = ChangeTypeVO.CHANGE_TYPE_CODES.getId(text);
        if (changeTypeId == -1)
            throw new TranslatableJsonException("emport.error.invalid",
                    "changeType", text,
                    ChangeTypeVO.CHANGE_TYPE_CODES.getCodeList());

        reader.readInto(getChangeType(), ctjson);
    }
}
