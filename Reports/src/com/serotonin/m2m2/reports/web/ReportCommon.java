/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports.web;

import com.serotonin.m2m2.reports.vo.ReportInstance;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;

public class ReportCommon {
    //
    //
    // Report access
    //
    public static void ensureReportPermission(User user, ReportVO report) throws RuntimeException {
        if (user == null)
            throw new RuntimeException("User does not exist");
        if (report == null)
            throw new RuntimeException("Report does not exist");
        if (report.getUserId() != user.getId())
            throw new PermissionException("User does not have permission to access the report", user);
    }

    public static void ensureReportInstancePermission(User user, ReportInstance instance) throws RuntimeException {
        if (user == null)
            throw new RuntimeException("User does not exist");
        if (instance == null)
            throw new RuntimeException("Report instance does not exist");
        if (instance.getUserId() != user.getId())
            throw new PermissionException("User does not have permission to access the report instance", user);
    }
}
