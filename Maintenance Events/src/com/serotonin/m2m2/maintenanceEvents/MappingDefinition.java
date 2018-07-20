/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.maintenanceEvents;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.serotonin.m2m2.module.MenuItemDefinition;

public class MappingDefinition extends MenuItemDefinition {

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MenuItemDefinition#getVisibility()
     */
    @Override
    public Visibility getVisibility() {
        return Visibility.ADMINISTRATOR;
    }
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MenuItemDefinition#getHref(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getHref(HttpServletRequest request, HttpServletResponse response) {
        return "/maintenance_events.shtm";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MenuItemDefinition#getTextKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getTextKey(HttpServletRequest request, HttpServletResponse response) {
        return "header.maintenanceEvents";
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.module.MenuItemDefinition#getImage(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getImage(HttpServletRequest request, HttpServletResponse response) {
        return "web/hammer.png";
    }
}
