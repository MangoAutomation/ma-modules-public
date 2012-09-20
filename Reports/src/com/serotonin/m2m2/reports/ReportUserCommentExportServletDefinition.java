/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import javax.servlet.http.HttpServlet;

import com.serotonin.m2m2.module.ServletDefinition;
import com.serotonin.m2m2.reports.servlet.ReportUserCommentExportServlet;

public class ReportUserCommentExportServletDefinition extends ServletDefinition {
    @Override
    public HttpServlet getServlet() {
        return new ReportUserCommentExportServlet();
    }

    @Override
    public String getUriPattern() {
        return "/userCommentExport/*";
    }
}
