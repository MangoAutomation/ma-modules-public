/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.pointLinks;

import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class PointLinkMappingDefinition extends UriMappingDefinition {
    
	@Override
    public String getPath() {
        return "/point_links.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return null;
    }

    @Override
    public String getJspPath() {
        return "web/pointLinks.jsp";
    }

    @Override
    public Permission getPermission() {
        return Permission.CUSTOM;
    }
    @Override
    public boolean hasCustomPermission(User user){
    	return Permissions.hasPermission(user, SystemSettingsDao.getValue(PointLinkPermissionDefinition.PERMISSION));
    }
}
