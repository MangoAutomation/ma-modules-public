/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.mango.rest.v2.util.ExceptionMapper;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.vo.User;

/**
 * @author Jared Wiltshire
 * @param <T> result type
 * @param <E> error type
 */
public abstract class TemporaryResourceManager<T, E> implements ExceptionMapper<E> {

    protected Log log = LogFactory.getLog(TemporaryResourceManager.class);

    /**
     * Default time before the resource is removed after completion
     */
    public static final long DEFAULT_EXPIRATION_MILLISECONDS = 300000; // 5 minutes

    /**
     * Default time that the task is allowed to run for before it is cancelled
     */
    public static final long DEFAULT_TIMEOUT_MILLISECONDS = 600000; // 10 minutes

    @FunctionalInterface
    public static interface ResourceTask<T, E> {
        public Consumer<TemporaryResource<T, E>> run(TemporaryResource<T, E> resource, User user) throws Exception;
    }

    private final ConcurrentMap<String, TemporaryResource<T, E>> resources;

    public TemporaryResourceManager() {
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
     * @param userId the user that started the temporary resource
     * @param expiration time after the resource completes that it will be removed (milliseconds). If null or less than zero, it will be set to the default DEFAULT_EXPIRATION_MILLISECONDS
     * @param timeout time after the resource starts that it will be timeout if not complete (milliseconds). If null or less than zero, it will be set to the default DEFAULT_TIMEOUT_MILLISECONDS
     * @param task the task to be run
     * @return
     */
    public final TemporaryResource<T, E> newTemporaryResource(String resourceType, String id, int userId, Long expiration, Long timeout, ResourceTask<T, E> task) {
        if (expiration == null || expiration < 0) {
            expiration = DEFAULT_EXPIRATION_MILLISECONDS;
        }
        if (timeout == null || timeout < 0) {
            timeout = DEFAULT_TIMEOUT_MILLISECONDS;
        }

        TemporaryResource<T, E> resource = new TemporaryResource<T, E>(resourceType, id, userId, expiration, timeout, task, this);
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
        return new ArrayList<>(this.resources.values());
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
