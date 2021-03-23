/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.latest.model.modules;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Container for a group of AngularJS Module Definitions
 *
 * @author Terry Packer
 */
public class AngularJSModuleDefinitionGroupModel {

    public static class ModuleInfo {
        String url;
        String name;
        String version;
        Date upgradedDate;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getVersion() {
            return version;
        }
        public void setVersion(String version) {
            this.version = version;
        }
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }

        public Date getUpgradedDate() {
            return upgradedDate;
        }

        public void setUpgradedDate(Date upgradedDate) {
            this.upgradedDate = upgradedDate;
        }
    }

    private List<String> urls = new ArrayList<>();
    private List<ModuleInfo> modules = new ArrayList<>();

    public void add(String urlWithVersion, ModuleInfo info) {
        this.urls.add(urlWithVersion);
        this.modules.add(info);
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<ModuleInfo> getModules() {
        return modules;
    }

    public void setModules(List<ModuleInfo> modules) {
        this.modules = modules;
    }

}
