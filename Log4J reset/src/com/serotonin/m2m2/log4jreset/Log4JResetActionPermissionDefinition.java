/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.serotonin.m2m2.log4jreset;

import com.serotonin.m2m2.module.PermissionDefinition;

/**
 *
 * @author Terry Packer
 */
public class Log4JResetActionPermissionDefinition extends PermissionDefinition {

    public static final String PERMISSION = "action.log4jUtil";

    @Override
    public String getPermissionKey() {
        return "log4JReset.settings.header";
    }

    @Override
    public String getPermissionTypeName() {
        return PERMISSION;
    }

}
