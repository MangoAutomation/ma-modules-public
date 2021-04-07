/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.mapping;

import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.vo.role.Role;

public class SingleMintermPermissionConverter implements Converter<String, MangoPermission> {

    private final PermissionService permissionService;

    public SingleMintermPermissionConverter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public MangoPermission convert(String value) {
        if (value.isEmpty()) {
            return MangoPermission.superadminOnly();
        }
        Set<Role> minterm = permissionService.splitMinterm(value);
        return new MangoPermission(Collections.singleton(minterm));
    }
}
