/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist.mobile;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.controller.ControllerUtils;

public class MobileLoginMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/mobile/login.htm";
    }

    @Override
    public UrlHandler getHandler() {
        return new UrlHandler() {
            @Override
            public View handleRequest(HttpServletRequest request, HttpServletResponse response,
                    Map<String, Object> model) {
                if ("POST".equals(request.getMethod())) {
                    // Form submission
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");

                    ProcessResult loginResult = ControllerUtils.tryLogin(request, username, password);

                    model.put("loginResult", loginResult);
                }

                return null;
            }
        };
    }

    @Override
    public String getJspPath() {
        return "web/mobile/login.jsp";
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
