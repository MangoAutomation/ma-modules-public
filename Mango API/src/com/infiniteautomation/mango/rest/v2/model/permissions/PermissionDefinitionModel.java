/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.permissions;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.PermissionDefinition;

/**
 * @author Terry Packer
 */
public class PermissionDefinitionModel {
    String systemSettingName;
    TranslatableMessage description;
    MangoPermissionModel permission;

    public PermissionDefinitionModel() {}

    public PermissionDefinitionModel(String systemSettingName, TranslatableMessage description, MangoPermission permission) {
        this.systemSettingName = systemSettingName;
        this.description = description;
        this.permission = new MangoPermissionModel(permission);
    }

    public PermissionDefinitionModel(PermissionDefinition def) {
        this.systemSettingName = def.getPermissionTypeName();
        this.description = def.getDescription();
        this.permission = new MangoPermissionModel(def.getPermission());
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

    public MangoPermissionModel getPermission() {
        return permission;
    }

    public void setPermission(MangoPermissionModel roles) {
        this.permission = roles;
    }
}
