/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.reports.web.ReportChartHandler;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class ReportChartMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/reportChart.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return new ReportChartHandler();
    }

    @Override
    public String getJspPath() {
        return null;
    }

    @Override
    public String getMenuKey() {
        return null;
    }

    @Override
    public String getMenuImage() {
        return null;
    }

    @Override
    public Permission getPermission() {
        return null;
    }
}
