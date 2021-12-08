package com.infiniteautomation.serial.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.infiniteautomation.serial.SerialDataSourceDefinition;
import com.infiniteautomation.serial.rt.SerialPointLocatorRT;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.util.SerializationHelper;

public class SerialPointLocatorVO extends AbstractPointLocatorVO<SerialPointLocatorVO> implements JsonSerializable{

    @Override
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public TranslatableMessage getConfigurationDescription() {
        //TODO add the properties to this
        return new TranslatableMessage("serial.point.configuration",pointIdentifier);
    }

    @Override
    public boolean isSettable() {
        return true;
    }

    @Override
    public SerialPointLocatorRT createRuntime() {
        return new SerialPointLocatorRT(this);
    }

    @Override
    public String getDataSourceType() {
        return SerialDataSourceDefinition.DATA_SOURCE_TYPE;
    }

    @JsonProperty
    private String pointIdentifier; //Address or unique ID in message for this point
    @JsonProperty
    private String valueRegex;
    @JsonProperty
    private int valueIndex;
    private DataType dataType = DataType.ALPHANUMERIC;

    public String getPointIdentifier() {
        return pointIdentifier;
    }

    public void setPointIdentifier(String pointIdentifier) {
        this.pointIdentifier = pointIdentifier;
    }

    public String getValueRegex() {
        return valueRegex;
    }

    public void setValueRegex(String valueRegex) {
        this.valueRegex = valueRegex;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, pointIdentifier);
        SerializationHelper.writeSafeUTF(out, valueRegex);
        out.writeInt(valueIndex);
        out.writeInt(dataType.getId());

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();
        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            pointIdentifier= SerializationHelper.readSafeUTF(in);
            valueRegex= SerializationHelper.readSafeUTF(in);
            valueIndex = in.readInt();
            dataType = DataType.fromId(in.readInt());
        }
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        if (jsonObject.containsKey("dataType")) {
            this.dataType = readDataType(jsonObject, DataType.IMAGE);
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writeDataType(writer);
    }
}
