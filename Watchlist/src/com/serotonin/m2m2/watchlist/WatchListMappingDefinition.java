/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class WatchListMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/watch_list.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return new WatchListHandler();
    }

    @Override
    public String getJspPath() {
        return "web/watchList.jsp";
    }

    @Override
    public String getMenuKey() {
        return "header.watchLists";
    }

    @Override
    public String getMenuImage() {
        return "web/eye.png";
    }

    @Override
    public Permission getPermission() {
        return Permission.USER;
    }
}
