/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
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
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.ImageSet;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
abstract public class ImageSetComponent extends PointComponent {
    protected ImageSet imageSet;
    @JsonProperty
    private boolean displayText;

    public ImageSet tgetImageSet() {
        return imageSet;
    }

    public void tsetImageSet(ImageSet imageSet) {
        this.imageSet = imageSet;
    }

    public boolean isDisplayText() {
        return displayText;
    }

    public void setDisplayText(boolean displayText) {
        this.displayText = displayText;
    }

    @Override
    public String snippetName() {
        return "imageContent";
    }

    abstract public String getImage(PointValueTime pointValue);

    public String defaultImage() {
        if (imageSet != null)
            return getImage(null);
        return null;
    }

    public int getHeight() {
        if (imageSet == null)
            return 0;
        return imageSet.getHeight();
    }

    public int getWidth() {
        if (imageSet == null)
            return 0;
        return imageSet.getWidth();
    }

    public int getTextX() {
        if (imageSet == null)
            return 0;
        return imageSet.getTextX();
    }

    public int getTextY() {
        if (imageSet == null)
            return 0;
        return imageSet.getTextY();
    }

    public String getImageSetId() {
        if (imageSet == null)
            return null;
        return imageSet.getId();
    }

    @Override
    public void addDataToModel(Map<String, Object> model, PointValueTime pointValue) {
        if (imageSet != null)
            model.put("image", getImage(pointValue));
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        if (imageSet == null)
            SerializationHelper.writeSafeUTF(out, null);
        else
            SerializationHelper.writeSafeUTF(out, imageSet.getId());
        out.writeBoolean(displayText);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            imageSet = Common.getImageSet(SerializationHelper.readSafeUTF(in));
            displayText = in.readBoolean();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);

        if (imageSet == null)
            writer.writeEntry("imageSet", null);
        else
            writer.writeEntry("imageSet", imageSet.getId());
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        JsonValue jsonImageId = jsonObject.get("imageSet");
        if (jsonImageId != null) {
            String id = jsonImageId.toString();
            imageSet = Common.getImageSet(id);
            if (imageSet == null)
                throw new TranslatableJsonException("emport.error.component.unknownImageSet", id,
                        Common.getImageSetIds());
        }
    }
}
