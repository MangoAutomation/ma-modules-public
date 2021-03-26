/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.permissions;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.module.PermissionGroup;

/**
 * @author Terry Packer
 */
public class PermissionDefinitionModel {
    private String name;
    private TranslatableMessage description;
    private MangoPermissionModel permission;
    private String groupName;
    private TranslatableMessage groupTitle;
    private TranslatableMessage groupDescription;

    public PermissionDefinitionModel() {}

    public PermissionDefinitionModel(PermissionDefinition def) {
        this.name = def.getPermissionTypeName();
        this.description = def.getDescription();
        this.permission = new MangoPermissionModel(def.getPermission());
        PermissionGroup group = def.getGroup();
        this.groupName = group.getName();
        this.groupTitle = group.getTitle();
        this.groupDescription = group.getDescription();
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public TranslatableMessage getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(TranslatableMessage groupTitle) {
        this.groupTitle = groupTitle;
    }

    public TranslatableMessage getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(TranslatableMessage groupDescription) {
        this.groupDescription = groupDescription;
    }
}
