/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.temporaryResource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.infiniteautomation.mango.rest.latest.util.ExceptionMapper;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

/**
 * @author Jared Wiltshire
 * @param <T> result type
 * @param <E> error type
 */
public abstract class TemporaryResourceManager<T, E> implements ExceptionMapper<E> {

    protected Logger log = LoggerFactory.getLogger(TemporaryResourceManager.class);
    protected final Environment environment;

    @FunctionalInterface
    public static interface ResourceTask<T, E> {
        public Consumer<TemporaryResource<T, E>> run(TemporaryResource<T, E> resource) throws Exception;
    }

    private final ConcurrentMap<String, TemporaryResource<T, E>> resources;
    private final PermissionService permissionService;

    public TemporaryResourceManager(PermissionService permissionService, Environment environment) {
        this.permissionService = permissionService;
        this.environment = environment;
        this.resources = new ConcurrentHashMap<>();
    }

    protected abstract void resourceAdded(TemporaryResource<T, E> resource);
    protected abstract void resourceRemoved(TemporaryResource<T, E> resource);
    protected abstract void resourceUpdated(TemporaryResource<T, E> resource);
    protected abstract void resourceCompleted(TemporaryResource<T, E> resource);

    /**
     * Creates a new temporary resource, how the task is executed depends on the manager implementation.
     *
     * @param resourceType unique type string assigned to each resource type e.g. BULK_DATA_POINT
     * @param id if null will be assigned a UUID
     * @param expiration time after the resource completes that it will be removed (milliseconds). If null or less than zero, it will be set to the default DEFAULT_EXPIRATION_MILLISECONDS
     * @param timeout time after the resource starts that it will be timeout if not complete (milliseconds). If null or less than zero, it will be set to the default DEFAULT_TIMEOUT_MILLISECONDS
     * @param task the task to be run
     * @return
     */
    public final TemporaryResource<T, E> newTemporaryResource(String resourceType, String id, Long expiration, Long timeout, ResourceTask<T, E> task) {
        if (expiration == null || expiration < 0) {

            expiration = Common.getMillis(
                    Common.TIME_PERIOD_CODES.getId(environment.getProperty("rest.temporaryResource.expirationPeriodType", "HOURS")),
                    environment.getProperty("rest.temporaryResource.expirationPeriods", Integer.class, 4));
        }
        if (timeout == null || timeout < 0) {
            timeout = Common.getMillis(
                    Common.TIME_PERIOD_CODES.getId(environment.getProperty("rest.temporaryResource.timeoutPeriodType", "HOURS")),
                    environment.getProperty("rest.temporaryResource.timeoutPeriods", Integer.class, 3));
        }

        PermissionHolder user = Common.getUser();
        TemporaryResource<T, E> resource = new TemporaryResource<T, E>(resourceType, id, user, expiration, timeout, task, this);
        synchronized (resource) {
            this.add(resource);
            resource.schedule();
            return resource;
        }
    }

    /**
     * @return list of all resources in the resources map
     */
    public final List<TemporaryResource<T, E>> list() {
        PermissionHolder user = Common.getUser();
        return this.resources.values().stream()
                .filter(r -> permissionService.hasAccessToResource(user, r))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Get a resource from the map of resources using its id
     * @param id
     * @return
     */
    public final TemporaryResource<T, E> get(String id) {
        TemporaryResource<T, E> resource = this.resources.get(id);
        if (resource == null) {
            throw new NotFoundException();
        }
        permissionService.ensureAccessToResource(Common.getUser(), resource);
        return resource;
    }

    /**
     * Adds the resource to the map of resources.
     * @param resource
     */
    private final void add(TemporaryResource<T, E> resource) {
        TemporaryResource<T, E> existing = this.resources.putIfAbsent(resource.getId(), resource);
        if (existing != null) {
            throw new RuntimeException("Resource id collision");
        }

        this.resourceAdded(resource);
    }

    /**
     * Removes the resource from the map of resources.
     * @param resource
     */
    final void remove(TemporaryResource<T, E> resource) {
        TemporaryResource<T, E> existing = this.resources.remove(resource.getId());
        if (existing == null) {
            throw new NotFoundException();
        }
        this.resourceRemoved(resource);
    }
}
