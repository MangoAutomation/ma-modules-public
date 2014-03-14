/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.watchlist.mobile;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class MobileWatchListMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/mobile/watch_list.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return new MobileWatchListHandler();
    }

    @Override
    public String getJspPath() {
        return "web/mobile/watchList.jsp";
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
        return null;
    }
}
