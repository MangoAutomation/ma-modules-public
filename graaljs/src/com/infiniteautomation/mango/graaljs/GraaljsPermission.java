/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.graaljs;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.module.PermissionGroup;

/**
 * Grants access to use the Graal.js scripting engine
 * @author Jared Wiltshire
 */
public class GraaljsPermission extends PermissionDefinition {

    public static final String PERMISSION = "script.graaljs";

    @Override
    public TranslatableMessage getDescription() {
        return new TranslatableMessage("permission." + PERMISSION);
    }

    @Override
    public String getPermissionTypeName() {
        return PERMISSION;
    }

    @Override
    public PermissionGroup getGroup() {
        return SCRIPTING_ENGINES_GROUP;
    }
}
