package com.serotonin.m2m2.dataImport;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class DataImportMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/dataImport.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return new DataImportController();
    }

    @Override
    public String getJspPath() {
        return "web/dataImport.jsp";
    }

    @Override
    public String getMenuKey() {
        return "dataImport.header";
    }

    @Override
    public String getMenuImage() {
        return "web/icon.png";
    }

    @Override
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }
}
