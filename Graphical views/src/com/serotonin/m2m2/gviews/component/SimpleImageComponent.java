/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import com.serotonin.json.spi.JsonEntity;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.view.ImplDefinition;

/**
 * @author Matthew Lohbihler
 */
@JsonEntity
public class SimpleImageComponent extends PointComponent {
    public static ImplDefinition DEFINITION = new ImplDefinition("simpleImage", "SIMPLE_IMAGE", "graphic.simpleImage",
            new int[] { DataTypes.IMAGE });

    @Override
    public String snippetName() {
        return "imageValueContent";
    }

    @Override
    public void addDataToModel(Map<String, Object> model, PointValueTime pointValue) {
        if (pointValue == null || pointValue.getValue() == null) {
            model.put("error", "common.noData");
            return;
        }

        if (!(pointValue.getValue() instanceof ImageValue)) {
            model.put("error", "common.thumb.invalidValue");
            return;
        }

        ImageValue imageValue = (ImageValue) pointValue.getValue();
        model.put("imageType", imageValue.getTypeExtension());
    }

    @Override
    public ImplDefinition definition() {
        return DEFINITION;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            // no op
        }
    }
}
