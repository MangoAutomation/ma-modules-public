/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.infiniteautomation.mango.rest.v2.bulk.BulkRequest;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.exception.NotFoundException;

/**
 * @author Jared Wiltshire
 * @param <T> result type
 * @param <E> error type
 */
public abstract class TemporaryResourceManager<T, E> {
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
        void run(TemporaryResource<T, E> resource) throws Exception;
    }

    private final ConcurrentMap<String, TemporaryResource<T, E>> resources;
    private final TemporaryResourceWebSocketHandler websocketHandler;

    public TemporaryResourceManager() {
        this(null);
    }
    
    public TemporaryResourceManager(TemporaryResourceWebSocketHandler websocketHandler) {
        this.websocketHandler = websocketHandler;
        this.resources = new ConcurrentHashMap<>();
    }
    
    public abstract E exceptionToError(Exception e);
    
    public final TemporaryResource<T, E> newTemporaryResource(BulkRequest<?, ?, ?> bulkRequest, User user, ResourceTask<T, E> resourceTask) {
        long expiration = DEFAULT_EXPIRATION_MILLISECONDS;
        long timeout = DEFAULT_TIMEOUT_MILLISECONDS;
        
        if (bulkRequest.getExpiration() != null && bulkRequest.getExpiration() >= 0) {
            expiration = bulkRequest.getExpiration();
        }
        if (bulkRequest.getTimeout() != null && bulkRequest.getTimeout() >= 0) {
            timeout = bulkRequest.getTimeout();
        }

        TemporaryResource<T, E> resource = new MangoTaskTemporaryResource<T, E>(bulkRequest.getId(), user.getId(), expiration, timeout, this, resourceTask);
        this.add(resource);

        try {
            resource.start();
        } catch (Exception e) {
            this.error(resource, e);
        }

        return resource;
    }

    public final List<TemporaryResource<T, E>> list() {
        return new ArrayList<>(this.resources.values());
    }
    
    public final TemporaryResource<T, E> get(String id) {
        TemporaryResource<T, E> resource = this.resources.get(id);
        if (resource == null) {
            throw new NotFoundException();
        }
        return resource;
    }
    
    public final void remove(TemporaryResource<T, E> resource) {
        this.resources.remove(resource.getId());
        resource.removed();
    }
    
    public final boolean cancel(TemporaryResource<T, E> resource) {
        boolean cancelled = false;
        
        try {
            cancelled = resource.cancel();
        } catch (Exception e) {
            this.error(resource, e);
        }

        if (cancelled && this.websocketHandler != null) {
            this.websocketHandler.notify(resource);
        }
        
        return cancelled;
    }

    final void add(TemporaryResource<T, E> resource) {
        TemporaryResource<T, E> existing = this.resources.putIfAbsent(resource.getId(), resource);
        if (existing != null) {
            throw new RuntimeException("UUID collision");
        }

        if (this.websocketHandler != null) {
            this.websocketHandler.notify(resource);
        }
    }

    final boolean timeOut(TemporaryResource<T, E> resource) {
        boolean timedOut = false;
        
        try {
            timedOut = resource.timeOut();
        } catch (Exception e) {
            this.error(resource, e);
        }
        
        if (timedOut && this.websocketHandler != null) {
            this.websocketHandler.notify(resource);
        }
        
        return timedOut;
    }

    public final boolean success(TemporaryResource<T, E> resource, T result) {
        boolean succeeded = false;
        
        try {
            succeeded = resource.success(result);
        } catch (Exception e) {
            this.error(resource, e);
        }
        
        if (succeeded && this.websocketHandler != null) {
            this.websocketHandler.notify(resource);
        }
        
        return succeeded;
    }
    
    public final void error(TemporaryResource<T, E> resource, Exception e) {
        E error = this.exceptionToError(e);
        this.error(resource, error);
    }
    
    public final boolean error(TemporaryResource<T, E> resource, E error) {
        if (resource.error(error)) {
            if (this.websocketHandler != null) {
                this.websocketHandler.notify(resource);
            }
            return true;
        }
        return false;
    }

    public final boolean progress(TemporaryResource<T, E> resource, T result, Integer position, Integer maximum) {
        boolean progressUpdated = resource.progress(result, position, maximum);
        if (progressUpdated && this.websocketHandler != null) {
            this.websocketHandler.notify(resource);
        }
        return progressUpdated;
    }
    
    /**
     * If position == maximum will set the resource to success, otherwise update progress
     * @param resource
     * @param result
     * @param position
     * @param maximum
     * @return
     */
    public final boolean progressOrSuccess(TemporaryResource<T, E> resource, T result, Integer position, Integer maximum) {
        boolean callSuccess = position != null && position.equals(maximum);

        boolean progressUpdated = resource.progress(result, position, maximum);
        if (progressUpdated && callSuccess) {
            progressUpdated = resource.success(result);
        }
        
        if (progressUpdated && this.websocketHandler != null) {
            this.websocketHandler.notify(resource);
        }

        return progressUpdated;
    }
}
