/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.NotFoundRestException;
import com.infiniteautomation.mango.rest.v2.model.permissions.PermissionDefinitionModel;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.vo.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * List of permissions and their keys
 *
 * @author Jared Wiltshire
 */
@Api(value="Lists permissions and their keys")
@PreAuthorize("isAdmin()")
@RestController
@RequestMapping("/permissions")
public class PermissionsRestController {

    @ApiOperation(
            value = "List permissions, their keys and roles"
            )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<PermissionDefinitionModel>> listPermissions() {
        List<PermissionDefinitionModel> permissions = new ArrayList<>();

        for (PermissionDefinition def : ModuleRegistry.getPermissionDefinitions().values()) {
            permissions.add(new PermissionDefinitionModel(def));
        }

        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Update all of a Permission's Roles",
            notes = "If no roles are supplied then all existing assigned roles are removed",
            response=PermissionDefinitionModel.class
            )
    @RequestMapping(method = RequestMethod.PUT, value="/{key}")
    public ResponseEntity<PermissionDefinitionModel> update(
            @PathVariable String key,
            @ApiParam(value="Permission", required=true)
            @RequestBody(required=true)
            PermissionDefinitionModel model,
            @AuthenticationPrincipal User user,
            HttpServletRequest request,
            UriComponentsBuilder builder,
            Authentication authentication){

        PermissionDefinition def = ModuleRegistry.getPermissionDefinition(key);

        if(def == null) {
            throw new NotFoundRestException();
        }

        def.update(model.getPermission() != null ? model.getPermission().getPermission() : null);

        URI location = builder.path("/permissions/{key}").buildAndExpand(key).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(new PermissionDefinitionModel(def), headers, HttpStatus.OK);
    }

}
