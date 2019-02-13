/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.pointLinks;

import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.SystemEventTypeDefinition;
import com.serotonin.web.taglib.Functions;

/**
 * @author Terry Packer
 *
 */
public class PointLinkAlreadyRunningEvent extends SystemEventTypeDefinition{
    public static final String TYPE_NAME = "POINT_LINK_ALREADY_RUNNING";

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return "event.system.pointLinkAlreadyRunning";
    }

    @Override
    public String getEventListLink(int ref1, int ref2, Translations translations) {
        String alt = Functions.quotEncode(translations.translate("events.editPointLink"));
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='point_links.shtm?plid=");
        sb.append(ref1);
        sb.append("'><img src='");
        sb.append(getModule().getWebPath()).append("/web/link.png");
        sb.append("' alt='").append(alt);
        sb.append("' title='").append(alt);
        sb.append("'/></a>");
        return sb.toString();
    }

    @Override
    public boolean supportsReferenceId1() {
        return true;
    }

    @Override
    public boolean supportsReferenceId2() {
        return false;
    }
}
