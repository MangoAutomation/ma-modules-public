/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
public class SimplePointComponent extends PointComponent {
    public static ImplDefinition DEFINITION = new ImplDefinition("simple", "SIMPLE", "graphic.simple", new int[] {
            DataTypes.BINARY, DataTypes.MULTISTATE, DataTypes.NUMERIC, DataTypes.ALPHANUMERIC });

    @JsonProperty
    private boolean displayPointName;

    @JsonProperty
    private String styleAttribute;

    public boolean isDisplayPointName() {
        return displayPointName;
    }

    public void setDisplayPointName(boolean displayPointName) {
        this.displayPointName = displayPointName;
    }

    public String getStyleAttribute() {
        return styleAttribute;
    }

    public void setStyleAttribute(String styleAttribute) {
        this.styleAttribute = styleAttribute;
    }

    @Override
    public String snippetName() {
        return "basicContent";
    }

    @Override
    public void addDataToModel(Map<String, Object> model, PointValueTime pointValue) {
        model.put("displayPointName", displayPointName);
        model.put("styleAttribute", styleAttribute);
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
    private static final int version = 3;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);

        out.writeBoolean(displayPointName);
        SerializationHelper.writeSafeUTF(out, styleAttribute);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            displayPointName = false;
            styleAttribute = "";
        }
        else if (ver == 2) {
            displayPointName = in.readBoolean();
            styleAttribute = "";
        }
        else if (ver == 3) {
            displayPointName = in.readBoolean();
            styleAttribute = SerializationHelper.readSafeUTF(in);
        }
    }
}
