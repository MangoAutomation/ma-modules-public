/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.model.system.actions.SystemActionResult;
import com.infiniteautomation.mango.rest.latest.model.system.actions.SystemActionTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Base class to help implement system actions
 * 
 * @author Terry Packer
 *
 */
@Api(value="System Actions Status")
@RestController
public class SystemActionsRestController {

    private final SystemActionTemporaryResourceManager manager;

    @Autowired
    public SystemActionsRestController(SystemActionTemporaryResourceManager manager){
        this.manager = manager;
    }
    
    /**
     * Get the status for a result
     */
    @ApiOperation(value = "Get Action Progress", notes = "Polls temporary resource for results.")
    @RequestMapping( method = {RequestMethod.GET}, value = {"/system-actions/status/{id}"})
    public TemporaryResource<SystemActionResult, AbstractRestException> getStatus(
            @ApiParam(value = "Valid running action id", required = true, allowMultiple = false)
            @PathVariable String id) {
        return manager.getStatus(id);
    }
    
    /**
     * Cancel/Delete a temporary resource for a system action
     */
    @ApiOperation(value = "Cancel a system action", notes = "Cancels action and removes resource.")
    @RequestMapping( method = {RequestMethod.DELETE}, value = {"/system-actions/status/{id}"})
    public TemporaryResource<SystemActionResult, AbstractRestException> cancel(
            @ApiParam(value = "Valid running action id", required = true, allowMultiple = false)
            @PathVariable String id) {
        return manager.cancel(id);
    }
    
}
