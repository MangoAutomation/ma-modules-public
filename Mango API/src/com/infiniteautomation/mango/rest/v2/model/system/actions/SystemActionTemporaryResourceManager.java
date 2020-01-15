/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model.system.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceManager.ResourceTask;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.PermissionDefinition;
import com.serotonin.m2m2.vo.User;

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

    protected final TemporaryResourceManager<SystemActionResult, AbstractRestV2Exception> resourceManager;
    protected final PermissionService service;

    @Autowired
    public SystemActionTemporaryResourceManager(TemporaryResourceWebSocketHandler websocket, PermissionService service) {
        this.resourceManager = new MangoTaskTemporaryResourceManager<>(service);
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
    public <T extends SystemActionResult> ResponseEntity<TemporaryResource<T, AbstractRestV2Exception>> create(
            SystemActionModel requestBody,
            User user,
            UriComponentsBuilder builder,
            String permissionTypeName,
            String resourceType,
            ResourceTask<SystemActionResult, AbstractRestV2Exception> task){
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
            user.ensureHasAdminRole();
        }

        @SuppressWarnings("unchecked")
        TemporaryResource<T, AbstractRestV2Exception> responseBody = (TemporaryResource<T, AbstractRestV2Exception>) resourceManager.newTemporaryResource(
                resourceType, null, user.getId(), expiration, timeout, task);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/system-actions/status/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<T, AbstractRestV2Exception>>(responseBody, headers, HttpStatus.CREATED);
    }

    /**
     * Get the status for a result
     * @param id
     * @param user
     * @return
     */
    public TemporaryResource<SystemActionResult, AbstractRestV2Exception> getStatus(String id, User user) {
        TemporaryResource<SystemActionResult, AbstractRestV2Exception> resource = resourceManager.get(id);

        if (!user.hasAdminRole() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        return resource;
    }

    /**
     * Cancel/Delete a temporary resource for a system action
     * @param id
     * @param user
     * @return
     */
    public TemporaryResource<SystemActionResult, AbstractRestV2Exception> cancel(String id, User user) {
        TemporaryResource<SystemActionResult, AbstractRestV2Exception> resource = resourceManager.get(id);

        if (!user.hasAdminRole() && user.getId() != resource.getUserId()) {
            throw new AccessDeniedException();
        }

        if(!resource.isComplete())
            resource.cancel();
        resource.remove();

        return resource;
    }
}
