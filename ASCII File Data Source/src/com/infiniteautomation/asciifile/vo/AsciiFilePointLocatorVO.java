package com.infiniteautomation.asciifile.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.infiniteautomation.asciifile.AsciiFileDataSourceDefinition;
import com.infiniteautomation.asciifile.rt.AsciiFilePointLocatorRT;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.util.SerializationHelper;

/**
 * @author Phillip Dunlap
 */

public class AsciiFilePointLocatorVO extends AbstractPointLocatorVO<AsciiFilePointLocatorVO> implements JsonSerializable{

    private static final long serialVersionUID = 1L;

    @Override
    public TranslatableMessage getConfigurationDescription() {
        //TODO add the properties to this
        return new TranslatableMessage("file.point.configuration",pointIdentifier);
    }

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public AsciiFilePointLocatorRT createRuntime() {
        return new AsciiFilePointLocatorRT(this);
    }

    @JsonProperty
    private String pointIdentifier; //Address or unique ID in message for this point
    @JsonProperty
    private String valueRegex;
    @JsonProperty
    private int pointIdentifierIndex;
    @JsonProperty
    private int valueIndex;
    private DataType dataType;
    @JsonProperty
    private boolean hasTimestamp;
    @JsonProperty
    private int timestampIndex;
    @JsonProperty
    private String timestampFormat;

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

    public int getPointIdentifierIndex() {
        return pointIdentifierIndex;
    }

    public void setPointIdentifierIndex(int pointIdentifierIndex) {
        this.pointIdentifierIndex = pointIdentifierIndex;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }

    @Override
    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public boolean getHasTimestamp() {
        return hasTimestamp;
    }

    public void setHasTimestamp(boolean hasTimestamp) {
        this.hasTimestamp = hasTimestamp;
    }

    public int getTimestampIndex() {
        return timestampIndex;
    }

    public void setTimestampIndex(int timestampIndex) {
        this.timestampIndex = timestampIndex;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    //
    //
    // Serialization
    //
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, pointIdentifier);
        SerializationHelper.writeSafeUTF(out, valueRegex);
        out.writeInt(pointIdentifierIndex);
        out.writeInt(valueIndex);
        out.writeInt(dataType.getId());
        out.writeBoolean(hasTimestamp);
        out.writeInt(timestampIndex);
        SerializationHelper.writeSafeUTF(out, timestampFormat);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();
        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            pointIdentifier= SerializationHelper.readSafeUTF(in);
            valueRegex= SerializationHelper.readSafeUTF(in);
            pointIdentifierIndex = in.readInt();
            valueIndex = in.readInt();
            dataType = DataType.fromId(in.readInt());
            hasTimestamp = false;
            timestampIndex = 0;
            timestampFormat = "";
        }
        if (ver == 2) {
            pointIdentifier= SerializationHelper.readSafeUTF(in);
            valueRegex= SerializationHelper.readSafeUTF(in);
            pointIdentifierIndex = in.readInt();
            valueIndex = in.readInt();
            dataType = DataType.fromId(in.readInt());
            hasTimestamp = in.readBoolean();
            timestampIndex = in.readInt();
            timestampFormat = SerializationHelper.readSafeUTF(in);
        }
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jo) throws JsonException {
        String text = jo.getString("dataType");
        if (text == null) {
            throw new TranslatableJsonException("emport.error.missing", "dataType", DataType.formatNames());
        }

        try {
            this.dataType = DataType.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new TranslatableJsonException("emport.error.invalid", "dataType", text, DataType.formatNames());
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("dataType", dataType.name());
    }

    @Override
    public String getDataSourceType() {
        return AsciiFileDataSourceDefinition.DATA_SOURCE_TYPE;
    }
}
