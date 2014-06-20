package com.serotonin.m2m2.reports;

import javax.servlet.http.HttpServlet;

import com.serotonin.m2m2.module.ServletDefinition;
import com.serotonin.m2m2.reports.servlet.ReportLaunchServlet;

public class ReportLaunchServletDefinition extends ServletDefinition {

	@Override
	public HttpServlet getServlet() {
		return new ReportLaunchServlet();
	}

	@Override
	public String getUriPattern() {
		return "/reportLaunch.shtm";
	}
}
