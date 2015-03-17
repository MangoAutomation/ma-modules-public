package com.serotonin.m2m2.internal.threads;

import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class ThreadsUriMapping extends UriMappingDefinition {
    @Override
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public String getPath() {
        return "/internal/threads.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return null;
    }

    @Override
    public String getJspPath() {
        return "web/threads.jsp";
    }
}
