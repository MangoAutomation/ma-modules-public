/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.web.taglib.Functions;

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
    public List<EventTypeVO> getEventTypeVOs() {
        List<EventTypeVO> vos = new ArrayList<EventTypeVO>();

        for (MaintenanceEventVO me : new MaintenanceEventDao().getMaintenanceEvents())
            vos.add(me.getEventType());

        return vos;
    }

    @Override
    public String getIconPath() {
        return getModule().getWebPath() + "/web/hammer.png";
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
        sb.append(getModule().getWebPath()).append("/web/hammer.png");
        sb.append("' alt='").append(alt);
        sb.append("' title='").append(alt);
        sb.append("'/></a>");
        return sb.toString();
    }

    @Override
    public TranslatableMessage getSourceDisabledMessage() {
        return new TranslatableMessage("event.rtn.maintDisabled");
    }
}
