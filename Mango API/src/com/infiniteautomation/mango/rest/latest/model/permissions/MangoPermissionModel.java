/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.model.permissions;

import com.infiniteautomation.mango.permission.MangoPermission;

/**
 *
 * @author Terry Packer
 */
public class MangoPermissionModel {

    private MangoPermission permission;

    public MangoPermissionModel() {
        this.permission = new MangoPermission();
    }

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
