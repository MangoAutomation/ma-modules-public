/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.pointLinks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.module.MenuItemDefinition;
import com.serotonin.m2m2.vo.permission.Permissions;

/**
 * @author Terry Packer
 *
 */
public class PointLinkMenuItemDefinition extends MenuItemDefinition{

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.MenuItemDefinition#getVisibility()
	 */
	@Override
	public Visibility getVisibility() {
		return Visibility.DATA_SOURCE;
	}
	
    @Override
    public boolean isVisible(HttpServletRequest request, HttpServletResponse response) {
    	return Permissions.hasPermission(Common.getUser(request), SystemSettingsDao.getValue(PointLinkPermissionDefinition.PERMISSION));
    }
    
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MenuItemDefinition#getHref(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getHref(HttpServletRequest request,
    		HttpServletResponse response) {
    	return "/point_links.shtm";
    }
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.MenuItemDefinition#getTextKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public String getTextKey(HttpServletRequest request,
			HttpServletResponse response) {
		  return "header.pointLinks";
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.MenuItemDefinition#getImage(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public String getImage(HttpServletRequest request,
			HttpServletResponse response) {
		return "web/link.png";
	}
}
