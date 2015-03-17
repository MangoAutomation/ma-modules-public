package com.serotonin.m2m2.internal;

import com.serotonin.m2m2.module.PermissionDefinition;

public class StatusPermissionDef extends PermissionDefinition {
    @Override
    public String getPermissionKey() {
        return "internal.status";
    }

    @Override
    public String getPermissionTypeName() {
        return "internal.status";
    }
}
