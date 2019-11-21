/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.watchlist;

import com.serotonin.m2m2.Constants;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.AuditEventTypeDefinition;
import com.serotonin.web.taglib.Functions;

/**
 * @author Terry Packer
 *
 */
public class AuditEvent extends AuditEventTypeDefinition {
    public static final String TYPE_NAME = "WATCH_LIST";

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return "event.audit.watchlist";
    }

    @Override
    public String getEventListLink(int ref1, int ref2, Translations translations) {
        String alt = Functions.quotEncode(translations.translate("events.editPointLink"));
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='watchlist.shtm?wlid=");
        sb.append(ref1);
        sb.append("'><img src='");
        sb.append("/" + Constants.DIR_MODULES + "/" + getModule().getName()).append("/web/link.png");
        sb.append("' alt='").append(alt);
        sb.append("' title='").append(alt);
        sb.append("'/></a>");
        return sb.toString();
    }
}
