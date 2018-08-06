/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports.web;

import java.io.File;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.reports.vo.ReportInstance;
import com.serotonin.m2m2.reports.vo.ReportVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.Permissions;

public class ReportCommon {
    public final File OVERRIDE_TEMPLATE_DIR = new File(Common.MA_HOME + "/overrides" + ModuleRegistry.getModule("reports").getDirectoryPath() + "/web/ftl/");
    public final File TEMPLATE_DIR = new File(Common.MA_HOME + ModuleRegistry.getModule("reports").getDirectoryPath() + "/web/ftl/");
    
    public static final ReportCommon instance = new ReportCommon();
    //
    //
    // Report access
    //
    public static void ensureReportPermission(User user, ReportVO report) throws RuntimeException {
        if (user == null)
            throw new RuntimeException("User does not exist");
        if (report == null)
            throw new RuntimeException("Report does not exist");
        if (report.getUserId() != user.getId() && ! Permissions.hasAdminPermission(user))
            throw new PermissionException(new TranslatableMessage("permission.reports.accessReport", user.getUsername()), user);
    }

    public static void ensureReportInstancePermission(User user, ReportInstance instance) throws RuntimeException {
        if (user == null)
            throw new RuntimeException("User does not exist");
        if (instance == null)
            throw new RuntimeException("Report instance does not exist");
        if (instance.getUserId() != user.getId() && ! Permissions.hasAdminPermission(user))
            throw new PermissionException(new TranslatableMessage("permission.reports.accessReportInstance", user.getUsername()), user);
    }
    
    public File getTemplateFile(String filename) {
        if (filename == null) return null;
        if(this.OVERRIDE_TEMPLATE_DIR.isDirectory()) {
            File override = new File(this.OVERRIDE_TEMPLATE_DIR, filename);
            if(override.exists())
                return override;
        }
        return new File(this.TEMPLATE_DIR, filename);
    }
}
