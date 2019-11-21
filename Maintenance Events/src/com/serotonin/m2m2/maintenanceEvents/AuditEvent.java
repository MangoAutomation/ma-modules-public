/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import com.serotonin.m2m2.Constants;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.AuditEventTypeDefinition;
import com.serotonin.web.taglib.Functions;

public class AuditEvent extends AuditEventTypeDefinition {
    public static final String TYPE_NAME = "MAINTENANCE_EVENT";

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return "event.audit.maintenanceEvent";
    }

    @Override
    public String getEventListLink(int ref1, int ref2, Translations translations) {
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
}
