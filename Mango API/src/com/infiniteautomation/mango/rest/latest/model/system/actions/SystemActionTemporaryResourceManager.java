/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.model.system.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceManager.ResourceTask;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * Class to manage system action results/feedback
 *
 * Autowire into System action controllers as necessary
 *
 * @author Terry Packer
 *
 */
@Component
public class SystemActionTemporaryResourceManager {

    protected final TemporaryResourceManager<SystemActionResult, AbstractRestException> resourceManager;
    protected final PermissionService service;

    @Autowired
    public SystemActionTemporaryResourceManager(TemporaryResourceWebSocketHandler websocket, PermissionService service) {
        this.resourceManager = new MangoTaskTemporaryResourceManager<>(service, websocket);
        this.service = service;
    }


    /**
     * Create the task
     * @param requestBody
     * @param user
     * @param builder
     * @param permissionTypeName - To get from system settings
     * @param resourceType
     * @param task
     * @return
     */
    public <T extends SystemActionResult> ResponseEntity<TemporaryResource<T, AbstractRestException>> create(
            SystemActionModel requestBody,
            PermissionHolder user,
            UriComponentsBuilder builder,
            String permissionTypeName,
            String resourceType,
            ResourceTask<SystemActionResult, AbstractRestException> task){
        requestBody.ensureValid();

        Long expiration = requestBody.getExpiration();
        Long timeout = requestBody.getTimeout();

        if(permissionTypeName != null) {
            PermissionDefinition def = ModuleRegistry.getPermissionDefinition(permissionTypeName);
            if(def == null) {
                throw new NotFoundException();
            }else {
                service.ensurePermission(user, def.getPermission());
            }
        }else {
            service.ensureAdminRole(user);
        }

        @SuppressWarnings("unchecked")
        TemporaryResource<T, AbstractRestException> responseBody = (TemporaryResource<T, AbstractRestException>) resourceManager.newTemporaryResource(
                resourceType, null, expiration, timeout, task);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/system-actions/status/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<T, AbstractRestException>>(responseBody, headers, HttpStatus.CREATED);
    }

    /**
     * Get the status for a result
     * @param id
     * @return
     */
    public TemporaryResource<SystemActionResult, AbstractRestException> getStatus(String id) {
        return resourceManager.get(id);
    }

    /**
     * Cancel/Delete a temporary resource for a system action
     * @param id
     * @return
     */
    public TemporaryResource<SystemActionResult, AbstractRestException> cancel(String id) {
        TemporaryResource<SystemActionResult, AbstractRestException> resource = resourceManager.get(id);
        if(!resource.isComplete())
            resource.cancel();
        resource.remove();
        return resource;
    }
}
