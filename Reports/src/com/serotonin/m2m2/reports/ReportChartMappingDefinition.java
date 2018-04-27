/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.reports;

import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.reports.web.ReportChartHandler;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class ReportChartMappingDefinition extends UriMappingDefinition {
    
    @Override
    public Permission getPermission() {
        return Permission.CUSTOM;
    }
    
    @Override
    public boolean hasCustomPermission(User user){
    	return Permissions.hasPermission(user, SystemSettingsDao.instance.getValue(ReportPermissionDefinition.PERMISSION));
    }

    @Override
    public String getPath() {
        return "/reportChart.shtm";
    }

    @Override
    public UrlHandler getHandler() {
    	return new ReportChartHandler();
    }

    @Override
    public String getJspPath() {
        return "web/status.jsp";
    }

}
