/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class ReportMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/reports.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return null;
    }

    @Override
    public String getJspPath() {
        return "web/reports.jsp";
    }

    @Override
    public String getMenuKey() {
        return "header.reports";
    }

    @Override
    public String getMenuImage() {
        return "web/report.png";
    }

    @Override
    public Permission getPermission() {
        return Permission.USER;
    }
}
