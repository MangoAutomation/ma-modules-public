/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
abstract public class PointComponent extends ViewComponent {
    private DataPointVO dataPoint;
    @JsonProperty
    private String nameOverride;
    @JsonProperty
    private boolean settableOverride;
    @JsonProperty
    private String bkgdColorOverride;
    @JsonProperty
    private boolean displayControls;

    // Runtime attributes
    private boolean valid;
    private boolean visible;

    @Override
    public boolean isPointComponent() {
        return true;
    }

    abstract public void addDataToModel(Map<String, Object> model, PointValueTime pointValue);

    abstract public String snippetName();

    @Override
    public void validateDataPoint(User user, boolean makeReadOnly) {
        if (dataPoint == null) {
            valid = false;
            visible = false;
        }
        else {
            visible = Permissions.hasDataPointReadPermission(user, dataPoint);
            valid = definition().supports(dataPoint.getPointLocator().getDataTypeId());
        }

        if (makeReadOnly)
            settableOverride = false;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean containsValidVisibleDataPoint(int dataPointId) {
        if (!valid || !visible)
            return false;

        return dataPoint.getId() == dataPointId;
    }

    public int[] getSupportedDataTypes() {
        return definition().getSupportedDataTypes();
    }

    public String getTypeName() {
        return definition().getName();
    }

    public TranslatableMessage getDisplayName() {
        return new TranslatableMessage(definition().getNameKey());
    }

    public String getName() {
        if (!StringUtils.isBlank(nameOverride))
            return nameOverride;
        if (dataPoint == null)
            return "(unknown)";
        return dataPoint.getName();
    }

    public boolean isSettable() {
        if (dataPoint == null)
            return false;
        if (!dataPoint.getPointLocator().isSettable())
            return false;
        return settableOverride;
    }

    public boolean isChartRenderer() {
        if (dataPoint == null)
            return false;
        return dataPoint.getChartRenderer() != null;
    }

    public DataPointVO tgetDataPoint() {
        return dataPoint;
    }

    public void tsetDataPoint(DataPointVO dataPoint) {
        this.dataPoint = dataPoint;
    }

    public int getDataPointId() {
        if (dataPoint == null)
            return 0;
        return dataPoint.getId();
    }

    public String getNameOverride() {
        return nameOverride;
    }

    public void setNameOverride(String nameOverride) {
        this.nameOverride = nameOverride;
    }

    public boolean isSettableOverride() {
        return settableOverride;
    }

    public void setSettableOverride(boolean settableOverride) {
        this.settableOverride = settableOverride;
    }

    public String getBkgdColorOverride() {
        return bkgdColorOverride;
    }

    public void setBkgdColorOverride(String bkgdColorOverride) {
        this.bkgdColorOverride = bkgdColorOverride;
    }

    public boolean isDisplayControls() {
        return displayControls;
    }

    public void setDisplayControls(boolean displayControls) {
        this.displayControls = displayControls;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);

        writeDataPoint(out, dataPoint);
        SerializationHelper.writeSafeUTF(out, nameOverride);
        out.writeBoolean(settableOverride);
        SerializationHelper.writeSafeUTF(out, bkgdColorOverride);
        out.writeBoolean(displayControls);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            dataPoint = readDataPoint(in);
            nameOverride = SerializationHelper.readSafeUTF(in);
            settableOverride = in.readBoolean();
            bkgdColorOverride = SerializationHelper.readSafeUTF(in);
            displayControls = in.readBoolean();
        }
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        jsonWriteDataPoint(writer, "dataPointXid", this);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
        jsonReadDataPoint(jsonObject.get("dataPointXid"), this);
    }
}
