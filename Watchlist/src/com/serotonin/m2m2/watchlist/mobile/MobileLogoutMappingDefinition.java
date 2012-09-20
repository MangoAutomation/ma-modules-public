/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist.mobile;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.controller.ControllerUtils;

public class MobileLogoutMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/mobile/logout.htm";
    }

    @Override
    public UrlHandler getHandler() {
        return new UrlHandler() {
            @Override
            public View handleRequest(HttpServletRequest request, HttpServletResponse response,
                    Map<String, Object> model) {
                ControllerUtils.doLogout(request);
                return null;
            }
        };
    }

    @Override
    public String getJspPath() {
        return "web/mobile/logout.jsp";
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
