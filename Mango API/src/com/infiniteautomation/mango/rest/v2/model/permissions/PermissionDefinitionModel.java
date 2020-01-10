/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.permissions;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.PermissionDefinition;

/**
 * @author Terry Packer
 */
public class PermissionDefinitionModel {
    String systemSettingName;
    TranslatableMessage description;

    public PermissionDefinitionModel() {}

    public PermissionDefinitionModel(String systemSettingName, TranslatableMessage description) {
        this.systemSettingName = systemSettingName;
        this.description = description;
    }

    public PermissionDefinitionModel(PermissionDefinition def) {
        this.systemSettingName = def.getPermissionTypeName();
        this.description = def.getDescription();
    }

    public String getSystemSettingName() {
        return systemSettingName;
    }

    public void setSystemSettingName(String systemSettingName) {
        this.systemSettingName = systemSettingName;
    }

    public TranslatableMessage getDescription() {
        return description;
    }

    public void setDescription(TranslatableMessage description) {
        this.description = description;
    }
}
