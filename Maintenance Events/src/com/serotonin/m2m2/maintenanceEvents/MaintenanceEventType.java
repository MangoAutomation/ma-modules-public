/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.io.IOException;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.rt.event.type.DuplicateHandling;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel;

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
    public DuplicateHandling getDuplicateHandling() {
        return DuplicateHandling.IGNORE;
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
        Integer id = MaintenanceEventDao.getInstance().getIdByXid(xid);
        if (id == null)
            throw new TranslatableJsonException("emport.error.eventType.invalid.reference", "XID", xid);
        maintenanceId = id;
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writer.writeEntry("XID", MaintenanceEventDao.getInstance().getXidById(maintenanceId));
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.rt.event.type.EventType#asModel()
     */
    @Override
    public EventTypeModel asModel() {
        return new MaintenanceEventTypeModel(this);
    }

    @Override
    public boolean hasPermission(PermissionHolder user) {
        MaintenanceEventVO vo = MaintenanceEventDao.getInstance().getFull(maintenanceId);
        if(vo == null)
            return false;
        else {
            for(int dsId : vo.getDataSources())
                if(!Permissions.hasDataSourcePermission(user, dsId))
                    return false;

            for(int dpId : vo.getDataPoints()) {
                DataPointVO dp = DataPointDao.getInstance().get(dpId);
                if(dp != null && !Permissions.hasDataPointReadPermission(user, dp))
                    return false;
            }
        }
        return true;
    }
}
