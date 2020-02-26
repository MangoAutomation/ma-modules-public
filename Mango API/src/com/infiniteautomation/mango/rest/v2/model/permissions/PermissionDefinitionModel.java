/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.permissions;

import java.util.HashSet;
import java.util.Set;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.vo.role.Role;

/**
 * @author Terry Packer
 */
public class PermissionDefinitionModel {
    String systemSettingName;
    TranslatableMessage description;
    Set<String> roles;

    public PermissionDefinitionModel() {}

    public PermissionDefinitionModel(String systemSettingName, TranslatableMessage description, Set<String> roles) {
        this.systemSettingName = systemSettingName;
        this.description = description;
        this.roles = roles;
    }

    public PermissionDefinitionModel(PermissionDefinition def) {
        this.systemSettingName = def.getPermissionTypeName();
        this.description = def.getDescription();
        Set<Role> defRoles = def.getRoles();
        this.roles = new HashSet<>();
        for(Role role : defRoles) {
            this.roles.add(role.getXid());
        }
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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
