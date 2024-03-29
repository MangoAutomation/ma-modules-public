/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.infiniteautomation.mango.rest.latest.model.system.SystemSettingTypeEnum;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.infiniteautomation.mango.util.exception.ValidationException;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Access to System Settings
 *
 * @author Terry Packer
 */
@Api(value="System Settings, configure/read System Settings")
@RestController
@RequestMapping("/system-settings")
public class SystemSettingsRestController {

    private SystemSettingsDao dao = SystemSettingsDao.getInstance();
    private final PermissionService permissionService;

    @Autowired
    public SystemSettingsRestController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @ApiOperation(
            value = "Get System Setting By key",
            notes = "Admin Permission Required, if no type supplied assume to be string"
            )
    @RequestMapping(method = RequestMethod.GET, value = "/{key}")
    public Object get(
            @ApiParam(value = "Valid System Setting ID", required = true, allowMultiple = false)
            @PathVariable String key,
            @ApiParam(value = "Return Type", required = false, defaultValue="STRING", allowMultiple = false)
            @RequestParam(required=false, defaultValue="STRING") SystemSettingTypeEnum type,
            @AuthenticationPrincipal PermissionHolder user) throws IOException {
        permissionService.ensureAdminRole(user);
        Object value = null;
        switch(type){
            case BOOLEAN:
                value = SystemSettingsDao.getInstance().getBooleanValue(key);
                break;
            case INTEGER:
                value = SystemSettingsDao.getInstance().getIntValue(key);
                break;
            case JSON:
                value = SystemSettingsDao.getInstance().getAsJson(key);
                break;
            case STRING:
            default:
                //First get the value as a String
                value = SystemSettingsDao.getInstance().getValue(key);
                try{
                    //Can it potentially be converted to an export code?
                    Integer i = Integer.parseInt((String) value);
                    value = SystemSettingsDao.getInstance().convertToCodeFromValue(key, i);
                    //Was it able to be converted?
                    if(value == null)
                        value = i.toString();
                }catch(NumberFormatException e){ }

                break;
        }
        if (value == null) {
            throw new NotFoundException();
        }else {
            return value;
        }
    }

    @ApiOperation(
            value = "Get All System Settings",
            notes = "Admin Permission Required, All settings returned as string types"
            )
    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> getAll(@AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);
        return dao.getAllSystemSettingsAsCodes();
    }

    @ApiOperation(
            value = "Update an existing System Setting",
            notes = "If no type is provided, String is assumed"
            )
    @RequestMapping(method = RequestMethod.PUT, value = "/{key}")
    public Object update(
            @PathVariable String key,
            @ApiParam(value = "Updated model", required = true)
            @RequestBody(required=true) JsonNode model,
            @AuthenticationPrincipal PermissionHolder user,
            UriComponentsBuilder builder, HttpServletRequest request) {
        permissionService.ensureAdminRole(user);
        ProcessResult response = new ProcessResult();
        Map<String, Object> settings = Collections.singletonMap(key, convertValue(model));

        settings = this.dao.convertCodesToValues(settings);

        this.dao.validate(settings, response, user);
        if (response.getHasMessages()) {
            throw new ValidationException(response);
        }
        this.dao.updateSettings(settings);
        return model;
    }

    private Map<String, Object> convertValues(Map<String, JsonNode> input) {
        Map<String, Object> result = new HashMap<>();
        for (Entry<String, JsonNode> entry : input.entrySet()) {
            result.put(entry.getKey(), convertValue(entry.getValue()));
        }
        return result;
    }

    private Object convertValue(JsonNode value) {
        if (value.isContainerNode()) {
            return value;
        } else if (value.isTextual()) {
            return value.textValue();
        } else if (value.isNumber()) {
            return value.intValue();
        } else if (value.isBoolean()) {
            return value.booleanValue();
        }
        return null;
    }

    @ApiOperation(
            value = "Update Many System Settings",
            notes = "Admin Privs Required"
            )
    @RequestMapping(method = RequestMethod.POST)
    public Map<String,Object> updateMany(
            @ApiParam(value = "Updated settings", required = true)
            @RequestBody(required=true) Map<String, JsonNode> body,
            @AuthenticationPrincipal PermissionHolder user) {
        permissionService.ensureAdminRole(user);
        ProcessResult response = new ProcessResult();
        Map<String, Object> settings = convertValues(body);

        // Convert incoming ExportCodes to int values
        settings = this.dao.convertCodesToValues(settings);
        this.dao.validate(settings, response, user);
        if (response.getHasMessages()) {
            throw new ValidationException(response);
        }
        this.dao.updateSettings(settings);
        return settings;
    }
}
