/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.io.IOException;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.rt.event.type.EventType;

public class MaintenanceEventType extends EventType {
    public static final String TYPE_NAME = "MAINTENANCE";

    private int maintenanceId;

    public MaintenanceEventType() {
        // Required for reflection.
    }

    public MaintenanceEventType(int maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    @Override
    public String getEventType() {
        return TYPE_NAME;
    }

    @Override
    public String getEventSubtype() {
        return null;
    }

    @Override
    public String toString() {
        return "MaintenanceEventType(maintenanceId=" + maintenanceId + ")";
    }

    @Override
    public int getDuplicateHandling() {
        return EventType.DuplicateHandling.IGNORE;
    }

    @Override
    public int getReferenceId1() {
        return maintenanceId;
    }

    @Override
    public int getReferenceId2() {
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + maintenanceId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MaintenanceEventType other = (MaintenanceEventType) obj;
        if (maintenanceId != other.maintenanceId)
            return false;
        return true;
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonRead(JsonReader reader, com.serotonin.json.type.JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        String xid = jsonObject.getString("XID");
        if (xid == null)
            throw new TranslatableJsonException("emport.error.eventType.missing.reference", "XID");
        MaintenanceEventVO me = new MaintenanceEventDao().getMaintenanceEvent(xid);
        if (me == null)
            throw new TranslatableJsonException("emport.error.eventType.invalid.reference", "XID", xid);
        maintenanceId = me.getId();
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writer.writeEntry("XID", new MaintenanceEventDao().getMaintenanceEvent(maintenanceId).getXid());
    }
}
