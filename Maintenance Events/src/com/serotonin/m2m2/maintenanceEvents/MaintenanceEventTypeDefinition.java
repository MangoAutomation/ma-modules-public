/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.infiniteautomation.mango.util.Functions;
import com.serotonin.m2m2.Constants;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel;

public class MaintenanceEventTypeDefinition extends EventTypeDefinition {
    @Override
    public String getTypeName() {
        return MaintenanceEventType.TYPE_NAME;
    }

    @Override
    public Class<? extends EventType> getEventTypeClass() {
        return MaintenanceEventType.class;
    }

    @Override
    public EventType createEventType(String subtype, int ref1, int ref2) {
        return new MaintenanceEventType(ref1);
    }

    @Override
    public boolean getHandlersRequireAdmin() {
        return true;
    }
    
    @Override
    public boolean hasCreatePermission(PermissionHolder user) {
        return user.hasAdminPermission();
    }

    @Override
    public List<EventTypeVO> getEventTypeVOs(PermissionHolder user) {
        List<EventTypeVO> vos = new ArrayList<EventTypeVO>();
        for (MaintenanceEventVO me : MaintenanceEventDao.getInstance().getAll())
            if(Permissions.hasEventTypePermission(user, me.getEventType().getEventType()))
                vos.add(me.getEventType());

        return vos;
    }

    @Override
    public String getIconPath() {
        return "/" + Constants.DIR_MODULES + "/" + getModule().getName() + "/web/hammer.png";
    }

    @Override
    public String getDescriptionKey() {
        return "maintenanceEvents.mes";
    }

    @Override
    public String getEventListLink(String subtype, int ref1, int ref2, Translations translations) {
        String alt = Functions.quotEncode(translations.translate("events.editMaintenanceEvent"));
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='maintenance_events.shtm?meid=");
        sb.append(ref1);
        sb.append("'><img src='");
        sb.append("/" + Constants.DIR_MODULES + "/" + getModule().getName()).append("/web/hammer.png");
        sb.append("' alt='").append(alt);
        sb.append("' title='").append(alt);
        sb.append("'/></a>");
        return sb.toString();
    }

    @Override
    public TranslatableMessage getSourceDisabledMessage() {
        return new TranslatableMessage("event.rtn.maintDisabled");
    }

	@Override
	public Class<? extends EventTypeModel> getModelClass() {
		return MaintenanceEventTypeModel.class;
	}
	
    @Override
    public List<String> getEventSubTypes(PermissionHolder user) {
        return Collections.emptyList();
    }

    @Override
    public boolean supportsReferenceId1() {
        return true;
    }

    @Override
    public boolean supportsReferenceId2() {
        return false;
    }

    @Override
    public boolean supportsSubType() {
        return false;
    }
    
    @Override
    public List<EventTypeVO> generatePossibleEventTypesWithReferenceId1(PermissionHolder user,
            String subtype) {
        List<EventTypeVO> vos = new ArrayList<EventTypeVO>();
        for (MaintenanceEventVO me : MaintenanceEventDao.getInstance().getAll())
        if(Permissions.hasEventTypePermission(user, me.getEventType().getEventType()) && StringUtils.equals(me.getEventType().getEventType().getEventSubtype(), subtype))
            vos.add(me.getEventType());
        return vos;
    }
}
