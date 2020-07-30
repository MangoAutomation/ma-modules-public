/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.permissions;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.Module;
import com.serotonin.m2m2.module.PermissionDefinition;

/**
 * @author Terry Packer
 */
public class PermissionDefinitionModel {
    private String name;
    private TranslatableMessage description;
    private MangoPermissionModel permission;
    private String moduleName;
    private TranslatableMessage moduleDescription;

    public PermissionDefinitionModel() {}

    public PermissionDefinitionModel(PermissionDefinition def) {
        this.name = def.getPermissionTypeName();
        this.description = def.getDescription();
        this.permission = new MangoPermissionModel(def.getPermission());
        Module module = def.getModule();
        this.moduleName = module.getName();
        this.moduleDescription = module.getDescription();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TranslatableMessage getDescription() {
        return description;
    }

    public MangoPermissionModel getPermission() {
        return permission;
    }

    public void setPermission(MangoPermissionModel roles) {
        this.permission = roles;
    }

    public String getModuleName() {
        return moduleName;
    }

    public TranslatableMessage getModuleDescription() {
        return moduleDescription;
    }

}
