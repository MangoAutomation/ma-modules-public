/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import com.serotonin.m2m2.module.SystemSettingsDefinition;

public class ReportSettingsDefinition extends SystemSettingsDefinition {
    @Override
    public String getDescriptionKey() {
        return "header.reports";
    }

    @Override
    public String getSectionJspPath() {
        return "web/settings.jspf";
    }
}
