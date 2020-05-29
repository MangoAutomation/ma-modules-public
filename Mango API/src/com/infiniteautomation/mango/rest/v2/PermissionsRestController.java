/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.permission.MangoPermission;
import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.FilteredStreamWithTotal;
import com.infiniteautomation.mango.rest.v2.model.ListWithTotal;
import com.infiniteautomation.mango.rest.v2.model.StreamWithTotal;
import com.infiniteautomation.mango.rest.v2.model.permissions.PermissionDefinitionModel;
import com.infiniteautomation.mango.spring.service.SystemPermissionService;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.PermissionDefinition;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.jazdw.rql.parser.ASTNode;

/**
 * List of permissions and their names
 *
 * @author Jared Wiltshire
 */
@Api(value = "Lists permissions and their names")
@PreAuthorize("isAdmin()")
@RestController
@RequestMapping("/system-permissions")
public class PermissionsRestController {

    private final SystemPermissionService service;

    @Autowired
    public PermissionsRestController(SystemPermissionService service) {
        this.service = service;
    }

    /**
     * For Swagger documentation use only.
     * @author Jared Wiltshire
     */
    private interface PermissionDefinitionQueryResult extends ListWithTotal<PermissionDefinitionModel> {
    }

    @ApiOperation(value = "Query permissions, their names and roles", response = PermissionDefinitionQueryResult.class)
    @RequestMapping(method = RequestMethod.GET)
    public StreamWithTotal<PermissionDefinitionModel> query(ASTNode rql) {
        List<PermissionDefinitionModel> permissions = new ArrayList<>();

        for (PermissionDefinition def : ModuleRegistry.getPermissionDefinitions().values()) {
            permissions.add(new PermissionDefinitionModel(def));
        }

        return new FilteredStreamWithTotal<>(permissions, rql);
    }

    @ApiOperation(value = "Update all of a Permission's Roles", notes = "If no roles are supplied then all existing assigned roles are removed")
    @RequestMapping(method = RequestMethod.PUT, value = "/{name}")
    public PermissionDefinitionModel update(@PathVariable String name,
            @ApiParam(value = "Permission", required = true) @RequestBody(required = true) PermissionDefinitionModel model) {

        PermissionDefinition def = ModuleRegistry.getPermissionDefinition(name);

        if (def == null) {
            throw new NotFoundRestException();
        }

        MangoPermission permission = model.getPermission() != null ? model.getPermission().getPermission() : null;
        service.update(permission, def);
        return new PermissionDefinitionModel(def);
    }

    @ApiOperation(value = "Get a permission")
    @RequestMapping(method = RequestMethod.GET, value = "/{name}")
    public PermissionDefinitionModel get(@PathVariable String name) {
        PermissionDefinition def = ModuleRegistry.getPermissionDefinition(name);
        if (def == null) {
            throw new NotFoundRestException();
        }
        return new PermissionDefinitionModel(def);
    }
}
