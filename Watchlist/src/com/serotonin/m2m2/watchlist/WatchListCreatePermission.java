/**
 * Copyright (C) 2020  Infinite Automation Software. All rights reserved.
 */

package com.serotonin.m2m2.watchlist;

import java.util.Collections;
import java.util.Set;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.vo.permission.PermissionHolder;
import com.serotonin.m2m2.vo.role.Role;

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
    protected Set<Role> getDefaultRoles() {
        return Collections.singleton(PermissionHolder.USER_ROLE);
    }

}
