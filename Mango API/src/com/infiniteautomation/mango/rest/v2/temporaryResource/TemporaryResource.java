/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.serotonin.m2m2.Common;

/**
 * @author Jared Wiltshire
 * @param <T> result type
 * @param <E> error type
 */
public abstract class TemporaryResource<T, E> {
    public static interface ShowResultView {}
    
    public static enum TemporaryResourceStatus {
        SCHEDULED, RUNNING, TIMED_OUT, CANCELLED, SUCCESS, ERROR;
    }
    
    private final String resourceType;
    private final String id;
    private final int userId;
    private final long expiration;
    private final long timeout;

    private TemporaryResourceStatus status;
    /**
     * Increments every time the status is changed or progress() is called
     */
    private int resourceVersion;
    @JsonView(ShowResultView.class)
    private T result;
    private E error;
    private Date startTime;
    private Date completionTime;
    private Integer position;
    private Integer maximum;

    /**
     * @param resourceType unique type string assigned to each resource type e.g. BULK_DATA_POINT
     * @param id if null will be assigned a UUID
     * @param userId user id of the user that started the temporary resource
     * @param expiration time after the resource completes that it will be removed (milliseconds)
     * @param timeout time after the resource starts that it will be timeout if not complete (milliseconds)
     */
    protected TemporaryResource(String resourceType, String id, int userId, long expiration, long timeout) {
        this.resourceType = resourceType;
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.userId = userId;
        this.expiration = expiration;
        this.timeout = timeout;

        this.status = TemporaryResourceStatus.SCHEDULED;
        this.resourceVersion = 0;
    }
    
    abstract void startTask();
    abstract void cancelMainAndTimeout();
    abstract void scheduleTimeout(Date timeout);
    abstract void scheduleRemoval(Date expiration);
    abstract void removeNow();
    abstract void cancelRemoval();
    
    synchronized final boolean start() {
        if (this.status == TemporaryResourceStatus.SCHEDULED) {
            this.status = TemporaryResourceStatus.RUNNING;
            this.resourceVersion++;
            this.startTime = new Date(Common.timer.currentTimeMillis());
            this.startTask();
            if (this.timeout > 0) {
                this.scheduleTimeout(new Date(this.startTime.getTime() + this.timeout));
            }
            return true;
        }
        return false;
    }

    synchronized final boolean progress(T result, Integer position, Integer maximum) {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.RUNNING;
            this.resourceVersion++;
            this.result = result;
            this.position = position;
            this.maximum = maximum;
            return true;
        }
        return false;
    }

    synchronized final boolean timeOut() {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.TIMED_OUT;
            this.resourceVersion++;
            this.completionTime = new Date(Common.timer.currentTimeMillis());
            if (this.expiration == 0) {
                this.removeNow();
            } else {
                this.scheduleRemoval(new Date(this.completionTime.getTime() + this.expiration));
            }
            this.cancelMainAndTimeout();
            return true;
        }
        return false;
    }

    synchronized final boolean cancel() {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.CANCELLED;
            this.resourceVersion++;
            this.completionTime = new Date(Common.timer.currentTimeMillis());
            if (this.expiration == 0) {
                this.removeNow();
            } else {
                this.scheduleRemoval(new Date(this.completionTime.getTime() + this.expiration));
            }
            this.cancelMainAndTimeout();
            return true;
        }
        return false;
    }

    synchronized final boolean success(T result) {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.SUCCESS;
            this.resourceVersion++;
            this.result = result;
            this.completionTime = new Date(Common.timer.currentTimeMillis());
            if (this.expiration == 0) {
                this.removeNow();
            } else {
                this.scheduleRemoval(new Date(this.completionTime.getTime() + this.expiration));
            }
            this.cancelMainAndTimeout();
            return true;
        }
        return false;
    }

    synchronized final boolean error(E error) {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.ERROR;
            this.resourceVersion++;
            this.error = error;
            this.completionTime = new Date(Common.timer.currentTimeMillis());
            if (this.expiration == 0) {
                this.removeNow();
            } else {
                this.scheduleRemoval(new Date(this.completionTime.getTime() + this.expiration));
            }
            this.cancelMainAndTimeout();
            return true;
        }
        return false;
    }
    
    synchronized final void removed() {
        this.cancelRemoval();
    }

    @JsonIgnore
    synchronized final boolean isComplete() {
        return !(this.status == TemporaryResourceStatus.SCHEDULED || this.status == TemporaryResourceStatus.RUNNING);
    }

    public final String getId() {
        return id;
    }

    public final TemporaryResourceStatus getStatus() {
        return status;
    }

    public final T getResult() {
        return result;
    }

    public final E getError() {
        return error;
    }

    public final Integer getPosition() {
        return position;
    }

    public final Integer getMaximum() {
        return maximum;
    }

    public final Integer getProgress() {
        if (position != null && maximum != null) {
            return Math.floorDiv(position * 100, maximum);
        }
        return null;
    }

    public final int getUserId() {
        return userId;
    }

    public int getResourceVersion() {
        return resourceVersion;
    }

    public String getResourceType() {
        return resourceType;
    }

    public long getExpiration() {
        return expiration;
    }

    public long getTimeout() {
        return timeout;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getCompletionTime() {
        return completionTime;
    }
}
