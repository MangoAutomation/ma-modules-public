/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.sqlConsole;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class SqlConsoleMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/sqlConsole.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return new SqlController();
    }

    @Override
    public String getJspPath() {
        return "web/sql.jsp";
    }

    @Override
    public String getMenuKey() {
        return "header.sql";
    }

    @Override
    public String getMenuImage() {
        return "web/sql.png";
    }

    @Override
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }
}
