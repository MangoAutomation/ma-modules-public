/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.web.mvc.rest.v1.model.PermissionDefinitionModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * List of permissions and their system setting keys
 * 
 * @author Jared Wiltshire
 */
@Api(value="Permissions", description="Lists permissions and their system setting keys")
@PreAuthorize("isAdmin()")
@RestController
@RequestMapping("/permissions")
public class PermissionsRestController {

    @ApiOperation(
        value = "List permissions and their system setting keys"
    )
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<PermissionDefinitionModel>> listPermissions() {
        List<PermissionDefinitionModel> permissions = new ArrayList<>();

        permissions.add(new PermissionDefinitionModel(SystemSettingsDao.PERMISSION_DATASOURCE, "systemSettings.permissions.datasourceManagement"));
        
        for (PermissionDefinition def : ModuleRegistry.getDefinitions(PermissionDefinition.class)) {
            permissions.add(new PermissionDefinitionModel(def));
        }
        
        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }

}
