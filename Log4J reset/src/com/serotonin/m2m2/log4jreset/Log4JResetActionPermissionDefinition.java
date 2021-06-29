/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.serotonin.m2m2.log4jreset;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.PermissionDefinition;

/**
 *
 * @author Terry Packer
 */
public class Log4JResetActionPermissionDefinition extends PermissionDefinition {

    public static final String PERMISSION = "action.log4jUtil";

    @Override
    public TranslatableMessage getDescription() {
        return new TranslatableMessage("log4JReset.settings.header");
    }

    @Override
    public String getPermissionTypeName() {
        return PERMISSION;
    }

}
