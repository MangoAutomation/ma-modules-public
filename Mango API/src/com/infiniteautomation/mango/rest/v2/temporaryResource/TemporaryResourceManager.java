/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.util.timeout.HighPriorityTask;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;
import com.serotonin.m2m2.vo.exception.NotFoundException;
import com.serotonin.timer.RejectedTaskReason;

/**
 * @author Jared Wiltshire
 */
public abstract class TemporaryResourceManager<T, E> {
    private final ConcurrentMap<String, TemporaryResource<T, E>> resources;
    
    public TemporaryResourceManager() {
        resources = new ConcurrentHashMap<>();
    }
    
    public abstract E exceptionToError(Exception e);
    
    public TemporaryResource<T, E> executeAsHighPriorityTask(int userId, int expirationSeconds, Consumer<TemporaryResource<T, E>> r) {
        AtomicReference<Runnable> cancelTask = new AtomicReference<>();
        
        TemporaryResource<T, E> resource = this.create(userId, expirationSeconds, () -> {
            Runnable cancel = cancelTask.get();
            if (r != null) cancel.run();
        });
        
        HighPriorityTask task = new HighPriorityTask(resource.getId()) {
            @Override
            public void run(long runtime) {
                try {
                    r.accept(resource);
                } catch (Exception e) {
                    E error = TemporaryResourceManager.this.exceptionToError(e);
                    TemporaryResourceManager.this.error(resource, error);
                }
            }

            @Override
            public void rejected(RejectedTaskReason reason) {
                super.rejected(reason);

                // TODO translation for rejection reason
                E error = TemporaryResourceManager.this.exceptionToError(new ServerErrorException());
                TemporaryResourceManager.this.error(resource, error);
            }
        };
        
        cancelTask.set(task::cancel);
        
        if (!resource.isComplete()) {
            Common.backgroundProcessing.execute(task);
        }
        
        return resource;
    }
    
    public List<TemporaryResource<T, E>> list() {
        return new ArrayList<>(this.resources.values());
    }

    public TemporaryResource<T, E> create(int userId, int expirationSeconds, Runnable cancel) {
        TemporaryResource<T, E> resource;
        TemporaryResource<T, E> existing;
        
        do {
            String id = UUID.randomUUID().toString();
            resource = new TemporaryResource<T, E>(id, userId, expirationSeconds, cancel);
            existing = this.resources.putIfAbsent(id, resource);
        } while (existing != null);
        
        return resource;
    }
    
    public TemporaryResource<T, E> get(String id) {
        TemporaryResource<T, E> resource = this.resources.get(id);
        if (resource == null) {
            throw new NotFoundException();
        }
        return resource;
    }
    
    public boolean remove(TemporaryResource<T, E> resource) {
        if (resource.isComplete()) {
            this.resources.remove(resource.getId());
            return true;
        }
        return false;
    }
    
    public boolean cancel(TemporaryResource<T, E> resource) {
        if (resource.cancel()) {
            // notify websocket
            this.scheduleRemoval(resource);
            return true;
        }
        return false;
    }
    
    public boolean timeOut(TemporaryResource<T, E> resource) {
        if (resource.timeOut()) {
            // notify websocket
            this.scheduleRemoval(resource);
            return true;
        }
        return false;
    }

    public boolean success(TemporaryResource<T, E> resource, T result) {
        if (resource.success(result)) {
            // notify websocket
            this.scheduleRemoval(resource);
            return true;
        }
        return false;
    }
    
    public boolean error(TemporaryResource<T, E> resource, E error) {
        if (resource.error(error)) {
            // notify websocket
            this.scheduleRemoval(resource);
            return true;
        }
        return false;
    }

    public boolean progress(TemporaryResource<T, E> resource, T result, Integer position, Integer maximum) {
        if (resource.progress(result, position, maximum)) {
            // notify websocket
            return true;
        }
        return false;
    }

    private void scheduleRemoval(TemporaryResource<T, E> resource) {
        String resourceId = resource.getId();
        Date expiration = resource.getExpiration();
        
        if (expiration != null) {
            // TimeoutTask schedules itself using Common.backgroundProcessing.schedule(this)
            new TimeoutTask(expiration, new TimeoutClient() {
                @Override
                public void scheduleTimeout(long fireTime) {
                    TemporaryResourceManager.this.resources.remove(resourceId);
                }
    
                @Override
                public String getThreadName() {
                    return "Temporary resource expiration " + resourceId;
                }
    
                @Override
                public void rejected(RejectedTaskReason reason) {
                    super.rejected(reason);
                    TemporaryResourceManager.this.resources.remove(resourceId);
                }
            });
        }
    }
}
