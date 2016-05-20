/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.internal.tasks;

import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.internal.StatusPermissionDef;
import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.UrlHandler;

/**
 * @author Terry Packer
 *
 */
public class TasksUriMapping extends UriMappingDefinition {
	
    @Override
    public Permission getPermission() {
        return Permission.CUSTOM;
    }
    
    @Override
    public boolean hasCustomPermission(User user){
    	return Permissions.hasPermission(user, SystemSettingsDao.getValue(StatusPermissionDef.PERMISSION));
    }

    @Override
    public String getPath() {
        return "/internal/tasks.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return null;
    }

    @Override
    public String getJspPath() {
        return "web/tasks.jsp";
    }

}
