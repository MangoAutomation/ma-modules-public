/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.dataImport;

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
public class DataImportMenuItemDefinition extends MenuItemDefinition {
    
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.MenuItemDefinition#getVisibility()
	 */
	@Override
	public Visibility getVisibility() {
		return Visibility.ADMINISTRATOR;
	}

	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.MenuItemDefinition#getTextKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public String getTextKey(HttpServletRequest request,
			HttpServletResponse response) {
		return "dataImport.header";
	}
	
	/* (non-Javadoc)
	 * @see com.serotonin.m2m2.module.MenuItemDefinition#getImage(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public String getImage(HttpServletRequest request,
			HttpServletResponse response) {
		return "web/icon.png";
	}

    /**
     * The value of the HTML href attribute to use in the menu item. If null, no attribute will be written.
     * 
     * @param request
     *            the current request
     * @param response
     *            the current response
     * @return the href value to use
     */
	@Override
    public String getHref(HttpServletRequest request, HttpServletResponse response) {
        return "/dataImport.shtm";
    }
	
    @Override
    public boolean isVisible(HttpServletRequest request, HttpServletResponse response) {
        return Permissions.hasPermission(Common.getHttpUser(), SystemSettingsDao.getValue(DataImportPermissionDefinition.PERMISSION));
    }
}
