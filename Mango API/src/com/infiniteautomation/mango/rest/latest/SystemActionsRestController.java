/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.model.system.actions.SystemActionResult;
import com.infiniteautomation.mango.rest.latest.model.system.actions.SystemActionTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;
import com.serotonin.m2m2.vo.User;

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
     * @param id
     * @param user
     * @return
     */
    @ApiOperation(value = "Get Action Progress", notes = "Polls temporary resource for results.")
    @RequestMapping( method = {RequestMethod.GET}, value = {"/system-actions/status/{id}"})
    public TemporaryResource<SystemActionResult, AbstractRestException> getStatus(
            @ApiParam(value = "Valid running action id", required = true, allowMultiple = false)
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        return manager.getStatus(id, user);
    }
    
    /**
     * Cancel/Delete a temporary resource for a system action
     * @param id
     * @param user
     * @return
     */
    @ApiOperation(value = "Cancel a system action", notes = "Cancels action and removes resource.")
    @RequestMapping( method = {RequestMethod.DELETE}, value = {"/system-actions/status/{id}"})
    public TemporaryResource<SystemActionResult, AbstractRestException> cancel(
            @ApiParam(value = "Valid running action id", required = true, allowMultiple = false)
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        return manager.cancel(id, user);
    }
    
}
