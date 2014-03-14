/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.DynamicImage;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
public class DynamicGraphicComponent extends PointComponent {
    public static ImplDefinition DEFINITION = new ImplDefinition("dynamicGraphic", "DYNAMIC_GRAPHIC",
            "graphic.dynamicGraphic", new int[] { DataTypes.NUMERIC });

    private DynamicImage dynamicImage;
    @JsonProperty
    private boolean displayText;
    @JsonProperty
    private double min;
    @JsonProperty
    private double max;

    public DynamicImage tgetDynamicImage() {
        return dynamicImage;
    }

    public void tsetDynamicImage(DynamicImage dynamicImage) {
        this.dynamicImage = dynamicImage;
    }

    public boolean isDisplayText() {
        return displayText;
    }

    public void setDisplayText(boolean displayText) {
        this.displayText = displayText;
    }

    public double getMax() {
        return max;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMin() {
        return min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    @Override
    public ImplDefinition definition() {
        return DEFINITION;
    }

    @Override
    public String snippetName() {
        return "dynamicImageContent";
    }

    public String getImage() {
        if (dynamicImage == null)
            return null;
        return dynamicImage.getImageFilename();
    }

    public double getProportion(PointValueTime pointValue) {
        if (pointValue == null || !(pointValue.getValue() instanceof NumericValue))
            return 0;

        double dvalue = pointValue.getDoubleValue();
        double proportion = (dvalue - min) / (max - min);
        if (proportion > 1)
            return 1;
        if (proportion < 0)
            return 0;
        return proportion;
    }

    public int getHeight() {
        if (dynamicImage == null)
            return 0;
        return dynamicImage.getHeight();
    }

    public int getWidth() {
        if (dynamicImage == null)
            return 0;
        return dynamicImage.getWidth();
    }

    public int getTextX() {
        if (dynamicImage == null)
            return 0;
        return dynamicImage.getTextX();
    }

    public int getTextY() {
        if (dynamicImage == null)
            return 0;
        return dynamicImage.getTextY();
    }

    public String getDynamicImageId() {
        if (dynamicImage == null)
            return null;
        return dynamicImage.getId();
    }

    @Override
    public void addDataToModel(Map<String, Object> model, PointValueTime pointValue) {
        model.put("proportion", getProportion(pointValue));
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
        if (dynamicImage == null)
            SerializationHelper.writeSafeUTF(out, null);
        else
            SerializationHelper.writeSafeUTF(out, dynamicImage.getId());
        out.writeDouble(min);
        out.writeDouble(max);
        out.writeBoolean(displayText);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            dynamicImage = Common.getDynamicImage(SerializationHelper.readSafeUTF(in));
            min = in.readDouble();
            max = in.readDouble();
            displayText = in.readBoolean();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);

        if (dynamicImage == null)
            writer.writeEntry("dynamicImage", null);
        else
            writer.writeEntry("dynamicImage", dynamicImage.getId());
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        JsonValue jsonImageId = jsonObject.get("dynamicImage");
        if (jsonImageId != null) {
            String id = jsonImageId.toString();
            dynamicImage = Common.getDynamicImage(id);
            if (dynamicImage == null)
                throw new TranslatableJsonException("emport.error.component.unknownDynamicImage", id,
                        Common.getDynamicImageIds());
        }
    }
}
