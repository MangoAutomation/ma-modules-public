/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class GraphicalViewsMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/views.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return new GraphicalViewHandler();
    }

    @Override
    public String getJspPath() {
        return "web/views.jsp";
    }

    @Override
    public String getMenuKey() {
        return "header.views";
    }

    @Override
    public String getMenuImage() {
        return "web/slide.png";
    }

    @Override
    public Permission getPermission() {
        return Permission.USER;
    }
}
