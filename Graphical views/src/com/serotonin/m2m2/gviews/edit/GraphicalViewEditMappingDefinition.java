/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.edit;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class GraphicalViewEditMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/view_edit.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return new GraphicalViewEditHandler();
    }

    @Override
    public String getJspPath() {
        return "web/viewEdit.jsp";
    }

    @Override
    public String getMenuKey() {
        return null;
    }

    @Override
    public String getMenuImage() {
        return null;
    }

    @Override
    public Permission getPermission() {
        return Permission.USER;
    }
}
