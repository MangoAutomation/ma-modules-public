/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.Constants;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.rest.v1.model.eventType.EventTypeModel;
import com.serotonin.web.taglib.Functions;

public class ScheduledEventTypeDefinition extends EventTypeDefinition {
    @Override
    public String getTypeName() {
        return ScheduledEventType.TYPE_NAME;
    }

    @Override
    public Class<? extends EventType> getEventTypeClass() {
        return ScheduledEventType.class;
    }

    @Override
    public EventType createEventType(String subTypeName, int ref1, int ref2) {
        return new ScheduledEventType(ref1);
    }

    @Override
    public boolean getHandlersRequireAdmin() {
        return false;
    }
    
    @Override
    public boolean hasCreatePermission(PermissionHolder user) {
        return true;
    }

    @Override
    public List<EventTypeVO> getEventTypeVOs(PermissionHolder user) {
        if(!user.hasAdminPermission())
            return Collections.emptyList();
        List<EventTypeVO> vos = new ArrayList<EventTypeVO>();
        for (ScheduledEventVO se : ScheduledEventDao.getInstance().getScheduledEvents())
            vos.add(se.getEventType());
        return vos;
    }

    @Override
    public String getIconPath() {
        return "/" + Constants.DIR_MODULES + "/" + getModule().getName() + "/web/clock.png";
    }

    @Override
    public String getDescriptionKey() {
        return "scheduledEvents.ses";
    }

    @Override
    public String getEventListLink(String subtype, int ref1, int ref2, Translations translations) {
        String alt = Functions.quotEncode(translations.translate("events.editScheduledEvent"));
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='scheduled_events.shtm?seid=");
        sb.append(ref1);
        sb.append("'><img src='");
        sb.append("/" + Constants.DIR_MODULES + "/" + getModule().getName()).append("/web/clock.png");
        sb.append("' alt='").append(alt);
        sb.append("' title='").append(alt);
        sb.append("'/></a>");
        return sb.toString();
    }

    @Override
    public TranslatableMessage getSourceDisabledMessage() {
        return null;
    }

	@Override
	public Class<? extends EventTypeModel> getModelClass() {
		return ScheduledEventTypeModel.class;
	}

    @Override
    public List<String> getEventSubTypes(PermissionHolder user) {
        return Collections.emptyList();
    }

    @Override
    public boolean supportsSubType() {
        return false;
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
    public List<EventTypeVO> generatePossibleEventTypesWithReferenceId1(PermissionHolder user,
            String subtype) {
        List<EventTypeVO> vos = new ArrayList<EventTypeVO>();
        for(ScheduledEventVO vo : ScheduledEventDao.getInstance().getScheduledEvents()) {
            if(Permissions.hasEventTypePermission(user, vo.getEventType().getEventType()) && StringUtils.equals(vo.getEventType().getEventType().getEventSubtype(), subtype))
                vos.add(vo.getEventType());
        }
        return vos;
    }
}
