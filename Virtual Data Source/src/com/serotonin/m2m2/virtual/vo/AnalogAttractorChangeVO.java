/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.virtual.vo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.virtual.rt.AnalogAttractorChangeRT;
import com.serotonin.m2m2.virtual.rt.ChangeTypeRT;
import com.serotonin.m2m2.vo.DataPointVO;

public class AnalogAttractorChangeVO extends ChangeTypeVO {
    public static final TranslatableMessage KEY = new TranslatableMessage("dsEdit.virtual.changeType.attractor");

    @JsonProperty
    private double maxChange;
    @JsonProperty
    private double volatility;
    private int attractionPointId;

    @Override
    public int typeId() {
        return Types.ANALOG_ATTRACTOR;
    }

    @Override
    public TranslatableMessage getDescription() {
        return KEY;
    }

    @Override
    public ChangeTypeRT createRuntime() {
        return new AnalogAttractorChangeRT(this);
    }

    public int getAttractionPointId() {
        return attractionPointId;
    }

    public void setAttractionPointId(int attractionPointId) {
        this.attractionPointId = attractionPointId;
    }

    public double getMaxChange() {
        return maxChange;
    }

    public void setMaxChange(double maxChange) {
        this.maxChange = maxChange;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeDouble(maxChange);
        out.writeDouble(volatility);
        out.writeInt(attractionPointId);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            maxChange = in.readDouble();
            volatility = in.readDouble();
            attractionPointId = in.readInt();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        DataPointVO dp = DataPointDao.instance.getDataPoint(attractionPointId);
        if (dp == null)
            writer.writeEntry("attractionPointId", null);
        else
            writer.writeEntry("attractionPointId", dp.getXid());
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
        String text = jsonObject.getString("attractionPointId");
        if (text != null) {
            DataPointVO dp = DataPointDao.instance.getDataPoint(text);
            if (dp == null)
                throw new TranslatableJsonException("virtual.error.attractor.missingPoint", "attractionPointId", text);
            attractionPointId = dp.getId();
        }
    }
}
