/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.infiniteautomation.mango.rest.v2.model.permissions;

import com.infiniteautomation.mango.permission.MangoPermission;

/**
 *
 * @author Terry Packer
 */
public class MangoPermissionModel {

    private MangoPermission permission;

    public MangoPermissionModel() { }

    public MangoPermissionModel(MangoPermission permission) {
        this.permission = permission;
    }

    public MangoPermission getPermission() {
        return permission;
    }

    public void setPermission(MangoPermission permission) {
        this.permission = permission;
    }
}
