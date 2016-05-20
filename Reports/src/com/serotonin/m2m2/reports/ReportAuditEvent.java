/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports;

import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.AuditEventTypeDefinition;
import com.serotonin.web.taglib.Functions;

/**
 * @author Terry Packer
 *
 */
public class ReportAuditEvent extends AuditEventTypeDefinition{

	public static final String TYPE_NAME = "REPORT";
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.AuditEventTypeDefinition#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return TYPE_NAME;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.AuditEventTypeDefinition#getDescriptionKey()
	 */
	@Override
	public String getDescriptionKey() {
		return "event.audit.report";
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.AuditEventTypeDefinition#getEventListLink(int, int, com.serotonin.m2m2.i18n.Translations)
	 */
	@Override
	public String getEventListLink(int ref1, int ref2, Translations translations) {
        String alt = Functions.quotEncode(translations.translate("event.audit.report"));
        StringBuilder sb = new StringBuilder();
        sb.append("<a href='/reports.shtm?reportId=");
        sb.append(ref1);
        sb.append("'><img src='");
        sb.append(getModule().getWebPath()).append("/web/images/report.png");
        sb.append("' alt='").append(alt);
        sb.append("' title='").append(alt);
        sb.append("'/></a>");
        return sb.toString();
	}

}