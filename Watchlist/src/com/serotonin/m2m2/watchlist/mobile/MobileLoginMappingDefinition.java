/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist.mobile;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.web.servlet.View;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableException;
import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.UrlHandler;

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

                    ProcessResult result = new ProcessResult();
                	try{
                		DataBinder binder = new DataBinder(User.class);
                		// Hack for now to get a BindException object so we can use the Auth
                		// Defs to login.
                		BindException errors = new BindException(binder.getBindingResult());
                		User user = Common.loginManager.performLogin(request.getParameter("username"), request.getParameter("password"), request, response, null, errors, false, false);
                		result.addData("user", user);
                		
                	}catch(TranslatableException e){
                		result.addMessage(e.getTranslatableMessage());
                	}
                    model.put("loginResult", result);
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
