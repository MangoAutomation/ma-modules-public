/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.serotonin.m2m2.watchlist;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 *
 * @author Terry Packer
 */
public class WatchListCreatePermission extends PermissionDefinition {

    public static final String PERMISSION = "watchList.create";

    @Override
    public TranslatableMessage getDescription() {
        return new TranslatableMessage("watchList.permission.create");
    }

    @Override
    public String getPermissionTypeName() {
        return PERMISSION;
    }

    @Override
    protected MangoPermission getDefaultPermission() {
        return MangoPermission.requireAnyRole(PermissionHolder.USER_ROLE);
    }

}
