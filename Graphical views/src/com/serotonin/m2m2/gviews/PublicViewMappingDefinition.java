/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class PublicViewMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/public_view.htm";
    }

    @Override
    public UrlHandler getHandler() {
        return new PublicViewHandler();
    }

    @Override
    public String getJspPath() {
        return "web/publicView.jsp";
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
        return Permission.ANONYMOUS;
    }
}
