/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.envcan;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.DataType;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.dataSource.AbstractPointLocatorVO;

/**
 * @author Matthew Lohbihler
 */
@JsonEntity
public class EnvCanPointLocatorVO extends AbstractPointLocatorVO<EnvCanPointLocatorVO> implements JsonSerializable {
    public interface Attributes {
        int TEMP = 1;
        int DEW_POINT_TEMP = 2;
        int REL_HUM = 3;
        int WIND_DIR = 4;
        int WIND_SPEED = 5;
        int VISIBILITY = 6;
        int STN_PRESS = 7;
        int HUMIDEX = 8;
        int WIND_CHILL = 9;
        int WEATHER = 10;
    }

    public static ExportCodes ATTRIBUTE_CODES = new ExportCodes();
    static {
        ATTRIBUTE_CODES.addElement(Attributes.TEMP, "TEMP", "envcands.attr.temp");
        ATTRIBUTE_CODES.addElement(Attributes.DEW_POINT_TEMP, "DEW_POINT_TEMP", "envcands.attr.dewPointTemp");
        ATTRIBUTE_CODES.addElement(Attributes.REL_HUM, "REL_HUM", "envcands.attr.relHum");
        ATTRIBUTE_CODES.addElement(Attributes.WIND_DIR, "WIND_DIR", "envcands.attr.windDir");
        ATTRIBUTE_CODES.addElement(Attributes.WIND_SPEED, "WIND_SPEED", "envcands.attr.windSpeed");
        ATTRIBUTE_CODES.addElement(Attributes.VISIBILITY, "VISIBILITY", "envcands.attr.visibility");
        ATTRIBUTE_CODES.addElement(Attributes.STN_PRESS, "STN_PRESS", "envcands.attr.stnPress");
        ATTRIBUTE_CODES.addElement(Attributes.HUMIDEX, "HUMIDEX", "envcands.attr.humidex");
        ATTRIBUTE_CODES.addElement(Attributes.WIND_CHILL, "WIND_CHILL", "envcands.attr.windChill");
        ATTRIBUTE_CODES.addElement(Attributes.WEATHER, "WEATHER", "envcands.attr.weather");
    };

    private int attributeId = Attributes.TEMP;

    @Override
    public boolean isSettable() {
        return false;
    }

    @Override
    public EnvCanPointLocatorRT createRuntime() {
        return new EnvCanPointLocatorRT(this);
    }

    @Override
    public TranslatableMessage getConfigurationDescription() {
        if (ATTRIBUTE_CODES.isValidId(attributeId))
            return new TranslatableMessage(ATTRIBUTE_CODES.getKey(attributeId));
        return new TranslatableMessage("common.unknown");
    }

    @Override
    public DataType getDataType() {
        if (attributeId == Attributes.WEATHER)
            return DataType.ALPHANUMERIC;
        return DataType.NUMERIC;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public String getDataSourceType() {
        return EnvCanDataSourceDefinition.DATA_SOURCE_TYPE;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(attributeId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1)
            attributeId = in.readInt();
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("attributeId", ATTRIBUTE_CODES.getCode(attributeId));
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        String text = jsonObject.getString("attributeId");
        if (text == null)
            throw new TranslatableJsonException("emport.error.missing", "attributeId", ATTRIBUTE_CODES.getCodeList());
        attributeId = ATTRIBUTE_CODES.getId(text);
        if (!ATTRIBUTE_CODES.isValidId(attributeId))
            throw new TranslatableJsonException("emport.error.invalid", "attributeId", text,
                    ATTRIBUTE_CODES.getCodeList());
    }

}
