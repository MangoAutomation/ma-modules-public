/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import javax.servlet.http.HttpServlet;

import com.serotonin.m2m2.module.ServletDefinition;
import com.serotonin.m2m2.reports.servlet.ReportChartServlet;

public class ReportChartServletDefinition extends ServletDefinition {
    @Override
    public HttpServlet getServlet() {
        return new ReportChartServlet();
    }

    @Override
    public String getUriPattern() {
        return "/reportImageChart/*";
    }
}
