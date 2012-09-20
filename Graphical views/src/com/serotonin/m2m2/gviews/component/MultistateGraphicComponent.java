/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.db.pair.IntStringPair;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.view.ImplDefinition;

/**
 * @author Matthew Lohbihler
 */
public class MultistateGraphicComponent extends ImageSetComponent {
    public static ImplDefinition DEFINITION = new ImplDefinition("multistateGraphic", "MULTISTATE_GRAPHIC",
            "graphic.multistateGraphic", new int[] { DataTypes.MULTISTATE });

    private Map<Integer, Integer> stateImageMap = new HashMap<Integer, Integer>();
    @JsonProperty
    private int defaultImage;

    public int getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(int defaultImage) {
        this.defaultImage = defaultImage;
    }

    @Override
    public ImplDefinition definition() {
        return DEFINITION;
    }

    @Override
    public String getImage(PointValueTime pointValue) {
        Integer state = null;
        if (pointValue != null && pointValue.getValue() instanceof MultistateValue)
            state = pointValue.getIntegerValue();

        Integer imageId = null;
        if (state != null)
            imageId = stateImageMap.get(state);

        if (imageId == null)
            imageId = defaultImage;

        if (imageId != null) {
            int id = imageId;

            if (id >= 0 && id < imageSet.getImageCount())
                return imageSet.getImageFilename(id);
        }

        return null;
    }

    public List<IntStringPair> getImageStateList() {
        List<IntStringPair> result = new ArrayList<IntStringPair>();
        for (Integer state : stateImageMap.keySet()) {
            Integer imageId = stateImageMap.get(state);

            IntStringPair stateList = null;
            for (IntStringPair ivp : result) {
                if (ivp.getKey() == imageId) {
                    stateList = ivp;
                    break;
                }
            }

            if (stateList == null) {
                stateList = new IntStringPair(imageId, state.toString());
                result.add(stateList);
            }
            else
                stateList.setValue(stateList.getValue() + ',' + state.toString());
        }
        return result;
    }

    public void setImageStateList(List<IntStringPair> imageStateList) {
        stateImageMap.clear();
        for (IntStringPair ivp : imageStateList) {
            String[] states = ivp.getValue().split(",");
            for (String stateStr : states) {
                try {
                    int state = Integer.parseInt(stateStr.trim());
                    stateImageMap.put(state, ivp.getKey());
                }
                catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public void validate(ProcessResult response) {
        super.validate(response);

        for (Integer index : stateImageMap.values()) {
            if (index < 0)
                response.addMessage("stateImageMappings", new TranslatableMessage("validate.cannotBeNegative"));
        }
        if (defaultImage < 0)
            response.addMessage("defaultImageIndex", new TranslatableMessage("validate.cannotBeNegative"));

        if (imageSet != null) {
            for (Integer index : stateImageMap.values()) {
                if (index >= imageSet.getImageCount())
                    response.addMessage("stateImageMappings", new TranslatableMessage(
                            "emport.error.component.imageIndex", index, imageSet.getId(), imageSet.getImageCount() - 1));
            }
            if (defaultImage >= imageSet.getImageCount())
                response.addMessage("defaultImageIndex", new TranslatableMessage("emport.error.component.imageIndex",
                        defaultImage, imageSet.getId(), imageSet.getImageCount() - 1));
        }
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

        out.writeObject(stateImageMap);
        out.writeInt(defaultImage);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            stateImageMap = (Map<Integer, Integer>) in.readObject();
            defaultImage = in.readInt();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);

        List<Map<String, Object>> jsonStateList = new ArrayList<Map<String, Object>>();
        for (Map.Entry<Integer, Integer> mapping : stateImageMap.entrySet()) {
            Map<String, Object> jsonMapping = new HashMap<String, Object>();
            jsonMapping.put("state", mapping.getKey());
            jsonMapping.put("imageIndex", mapping.getValue());
            jsonStateList.add(jsonMapping);
        }
        writer.writeEntry("stateImageMappings", jsonStateList);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        JsonArray jsonStateList = jsonObject.getJsonArray("stateImageMappings");
        if (jsonStateList != null) {
            stateImageMap.clear();

            for (JsonValue jv : jsonStateList) {
                JsonObject jsonMapping = jv.toJsonObject();
                Integer state = jsonMapping.getInt("state");
                if (state == null)
                    throw new TranslatableJsonException("emport.error.missingValue", "state");

                Integer index = jsonMapping.getInt("imageIndex");
                if (index == null)
                    throw new TranslatableJsonException("emport.error.missingValue", "index");

                stateImageMap.put(state, index);
            }
        }
    }
}
