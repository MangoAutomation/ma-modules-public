package com.serotonin.m2m2.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.module.MenuItemDefinition;
import com.serotonin.m2m2.vo.permission.Permissions;

public class InternalMenuItem extends MenuItemDefinition {
    @Override
    public Visibility getVisibility() {
        return Visibility.USER;
    }

    @Override
    public String getTextKey(HttpServletRequest request, HttpServletResponse response) {
        return "internal.status";
    }

    @Override
    public String getImage(HttpServletRequest request, HttpServletResponse response) {
        return "web/system-monitor.png";
    }

    @Override
    public String getHref(HttpServletRequest request, HttpServletResponse response) {
        return "/internal/status.shtm";
    }

    @Override
    public boolean isVisible(HttpServletRequest request, HttpServletResponse response) {
        return Permissions.hasPermission(Common.getUser(request), SystemSettingsDao.getValue(StatusPermissionDef.PERMISSION));
    }
}
