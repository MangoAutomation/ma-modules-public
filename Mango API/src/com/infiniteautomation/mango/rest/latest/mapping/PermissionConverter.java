/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */

package com.infiniteautomation.mango.rest.latest.mapping;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.vo.role.Role;

public class PermissionConverter implements Converter<String[], MangoPermission> {

    private final PermissionService permissionService;

    public PermissionConverter(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public MangoPermission convert(String[] value) {
        Set<Set<Role>> minterms = Arrays.stream(value)
            .map(permissionService::splitMinterm)
            .collect(Collectors.toSet());
        return new MangoPermission(minterms);
    }
}
