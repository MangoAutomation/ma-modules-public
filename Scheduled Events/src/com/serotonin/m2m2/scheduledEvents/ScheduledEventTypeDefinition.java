/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.scheduledEvents;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.vo.event.EventTypeVO;
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
    public List<EventTypeVO> getEventTypeVOs() {
        List<EventTypeVO> vos = new ArrayList<EventTypeVO>();

        for (ScheduledEventVO se : new ScheduledEventDao().getScheduledEvents())
            vos.add(se.getEventType());

        return vos;
    }

    @Override
    public String getIconPath() {
        return getModule().getWebPath() + "/web/clock.png";
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
        sb.append(getModule().getWebPath()).append("/web/clock.png");
        sb.append("' alt='").append(alt);
        sb.append("' title='").append(alt);
        sb.append("'/></a>");
        return sb.toString();
    }

    @Override
    public TranslatableMessage getSourceDisabledMessage() {
        return null;
    }
}
