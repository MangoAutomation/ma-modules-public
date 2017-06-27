/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */

package com.serotonin.m2m2.web.mvc.rest.v1.model;

import com.serotonin.m2m2.module.PermissionDefinition;

/**
 * @author Jared Wiltshire
 */
public class PermissionDefinitionModel {
    String systemSettingName;
    String translationKey;
    
    public PermissionDefinitionModel() {}
    
    public PermissionDefinitionModel(String systemSettingName, String translationKey) {
        this.systemSettingName = systemSettingName;
        this.translationKey = translationKey;
    }
    
    public PermissionDefinitionModel(PermissionDefinition def) {
        this.systemSettingName = def.getPermissionTypeName();
        this.translationKey = def.getPermissionKey();
    }

    public String getSystemSettingName() {
        return systemSettingName;
    }

    public void setSystemSettingName(String systemSettingName) {
        this.systemSettingName = systemSettingName;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }
}
