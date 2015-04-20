package com.serotonin.m2m2.internal.workItem;

import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class WorkItemUriMapping extends UriMappingDefinition {
    @Override
    public Permission getPermission() {
        return Permission.CUSTOM;
    }
    
    @Override
    public boolean hasCustomPermission(User user){
    	return Permissions.hasPermission(user, SystemSettingsDao.getValue("internal.status"));
    }

    @Override
    public String getPath() {
        return "/internal/workItems.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return null;
    }

    @Override
    public String getJspPath() {
        return "web/workItems.jsp";
    }
}
