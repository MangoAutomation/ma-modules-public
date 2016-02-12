/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.view.ImplDefinition;

/**
 * @author Matthew Lohbihler
 */
public class ImageChartComponent extends CompoundComponent {
    public static ImplDefinition DEFINITION = new ImplDefinition("imageChart", "IMAGE_CHART", "graphic.imageChart",
            null);

    public static final String POINT_1 = "point1";
    public static final String POINT_2 = "point2";
    public static final String POINT_3 = "point3";
    public static final String POINT_4 = "point4";
    public static final String POINT_5 = "point5";
    public static final String POINT_6 = "point6";
    public static final String POINT_7 = "point7";
    public static final String POINT_8 = "point8";
    public static final String POINT_9 = "point9";
    public static final String POINT_10 = "point10";

    @JsonProperty
    private int width = 500;
    @JsonProperty
    private int height = 300;
    private int durationType = Common.TimePeriods.DAYS;
    @JsonProperty
    private int durationPeriods = 1;

    public ImageChartComponent() {
    	super();
        initialize();
    }

    @Override
    protected void initialize() {
        addChild(POINT_1, "graphic.imageChart." + POINT_1, new SimplePointComponent(), null);
        addChild(POINT_2, "graphic.imageChart." + POINT_2, new SimplePointComponent(), null);
        addChild(POINT_3, "graphic.imageChart." + POINT_3, new SimplePointComponent(), null);
        addChild(POINT_4, "graphic.imageChart." + POINT_4, new SimplePointComponent(), null);
        addChild(POINT_5, "graphic.imageChart." + POINT_5, new SimplePointComponent(), null);
        addChild(POINT_6, "graphic.imageChart." + POINT_6, new SimplePointComponent(), null);
        addChild(POINT_7, "graphic.imageChart." + POINT_7, new SimplePointComponent(), null);
        addChild(POINT_8, "graphic.imageChart." + POINT_8, new SimplePointComponent(), null);
        addChild(POINT_9, "graphic.imageChart." + POINT_9, new SimplePointComponent(), null);
        addChild(POINT_10, "graphic.imageChart." + POINT_10, new SimplePointComponent(), null);
    }

    @Override
    public boolean hasInfo() {
        return true;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDurationType() {
        return durationType;
    }

    public void setDurationType(int durationType) {
        this.durationType = durationType;
    }

    public int getDurationPeriods() {
        return durationPeriods;
    }

    public void setDurationPeriods(int durationPeriods) {
        this.durationPeriods = durationPeriods;
    }

    @Override
    public ImplDefinition definition() {
        return DEFINITION;
    }

    @Override
    public String getStaticContent() {
        return null;
    }

    @Override
    public boolean isDisplayImageChart() {
        return false;
    }

    @Override
    public String getImageChartData(Translations translations) {
        return generateImageChartData(translations, Common.getMillis(durationType, durationPeriods), width, height,
                POINT_1, POINT_2, POINT_3, POINT_4, POINT_5, POINT_6, POINT_7, POINT_8, POINT_9, POINT_10);
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
        out.writeInt(width);
        out.writeInt(height);
        out.writeInt(durationType);
        out.writeInt(durationPeriods);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            width = in.readInt();
            height = in.readInt();
            durationType = in.readInt();
            durationPeriods = in.readInt();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writer.writeEntry("durationType", Common.TIME_PERIOD_CODES.getCode(durationType));
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        String text = jsonObject.getString("durationType");
        if (text == null)
            throw new TranslatableJsonException("emport.error.chart.missing", "durationType",
                    Common.TIME_PERIOD_CODES.getCodeList());

        durationType = Common.TIME_PERIOD_CODES.getId(text);
        if (durationType == -1)
            throw new TranslatableJsonException("emport.error.chart.invalid", "durationType", text,
                    Common.TIME_PERIOD_CODES.getCodeList());
    }
}
