/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import java.io.IOException;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel;

/**
 * @author Matthew Lohbihler
 *
 */
public class ScheduledEventType extends EventType {
    public static final String TYPE_NAME = "SCHEDULED";

    private int scheduleId;
    private int duplicateHandling = EventType.DuplicateHandling.IGNORE;

    public ScheduledEventType() {
        // Required for reflection.
    }

    public ScheduledEventType(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public ScheduledEventType(int scheduleId, int duplicateHandling) {
        this.scheduleId = scheduleId;
        this.duplicateHandling = duplicateHandling;
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
        return "ScheduledEventType(scheduleId=" + scheduleId + ")";
    }

    @Override
    public int getDuplicateHandling() {
        return duplicateHandling;
    }

    public void setDuplicateHandling(int duplicateHandling) {
        this.duplicateHandling = duplicateHandling;
    }

    @Override
    public int getReferenceId1() {
        return scheduleId;
    }

    @Override
    public int getReferenceId2() {
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + scheduleId;
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
        ScheduledEventType other = (ScheduledEventType) obj;
        if (scheduleId != other.scheduleId)
            return false;
        return true;
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        String xid = jsonObject.getString("XID");
        if (xid == null)
            throw new TranslatableJsonException("emport.error.eventType.missing.reference", "XID");
        ScheduledEventVO se = ScheduledEventDao.instance.getScheduledEvent(xid);
        if (se == null)
            throw new TranslatableJsonException("emport.error.eventType.invalid.reference", "XID", xid);
        scheduleId = se.getId();
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writer.writeEntry("XID", ScheduledEventDao.instance.getScheduledEvent(scheduleId).getXid());
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.rt.event.type.EventType#asModel()
     */
    @Override
    public EventTypeModel asModel() {
        return new ScheduledEventTypeModel(this);
    }

    @Override
    public boolean hasPermission(PermissionHolder user) {
        return Permissions.hasAdminPermission(user);
    }
}
