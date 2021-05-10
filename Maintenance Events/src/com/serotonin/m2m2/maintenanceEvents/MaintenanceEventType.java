/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.service.DataPointService;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.spring.service.maintenanceEvents.MaintenanceEventsService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.rt.event.type.DuplicateHandling;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.role.Role;

public class MaintenanceEventType extends EventType {
    public static final String TYPE_NAME = "MAINTENANCE";

    private int maintenanceId;

    public MaintenanceEventType() {
        // Required for reflection.
    }

    public MaintenanceEventType(int maintenanceId) {
        this.maintenanceId = maintenanceId;
        supplyReference1(() -> {
            return Common.getBean(MaintenanceEventDao.class).get(maintenanceId);
        });
    }

    public MaintenanceEventType(MaintenanceEventVO me) {
        this.maintenanceId = me.getId();
        supplyReference1(() -> {
            return me;
        });
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

    @Override
    public boolean hasPermission(PermissionHolder user, PermissionService service) {
        if(service.hasEventsSuperadminViewPermission(user)) {
            return true;
        }

        MaintenanceEventsService maintenanceEventService = Common.getBean(MaintenanceEventsService.class);
        try {
            MaintenanceEventVO vo = maintenanceEventService.get(maintenanceId);
            for(int dsId : vo.getDataSources()) {
                service.ensureDataSourceReadPermission(user, dsId);
            }
            for(int dpId : vo.getDataPoints()) {
                service.ensureDataPointReadPermission(user, dpId);
            }
        }catch(NotFoundException | PermissionException e) {
            return false;
        }
        return true;
    }

    @Override
    public MangoPermission getEventPermission(Map<String, Object> context, PermissionService service) {
        DataSourceService dataSourceService = Common.getBean(DataSourceService.class);
        DataPointService dataPointService = Common.getBean(DataPointService.class);
        MaintenanceEventsService maintenanceEventService = Common.getBean(MaintenanceEventsService.class);

        Set<Role> allRequired = new HashSet<>();
        try {
            MaintenanceEventVO vo = maintenanceEventService.get(maintenanceId);
            try {
                for (int dsId : vo.getDataSources()) {
                    MangoPermission read = dataSourceService.getReadPermission(dsId);
                    read.getRoles().forEach(allRequired::addAll);
                }
            } catch (NotFoundException e) {
                //Ignore this item
            }
            try {
                for (int dpId : vo.getDataPoints()) {
                    MangoPermission read = dataPointService.getReadPermission(dpId);
                    read.getRoles().forEach(allRequired::addAll);
                }
            } catch (NotFoundException e) {
                //Ignore this item
            }
        } catch (NotFoundException e) {
            //Ignore all of it
        }
        if (allRequired.size() == 0) {
            return MangoPermission.superadminOnly();
        } else {
            return MangoPermission.requireAllRoles(allRequired);
        }

    }
}
