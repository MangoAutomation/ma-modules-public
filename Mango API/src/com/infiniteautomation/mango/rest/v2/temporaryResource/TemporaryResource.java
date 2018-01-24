/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.m2m2.Common;

/**
 * @author Jared Wiltshire
 * @param <T> result type
 * @param <E> error type
 */
public abstract class TemporaryResource<T, E> {
    public static enum TemporaryResourceStatus {
        SCHEDULED, RUNNING, TIMED_OUT, CANCELLED, SUCCESS, ERROR;
    }
    
    private final String id;
    private final int userId;
    private final long expirationMilliseconds;
    private final Long timeoutMilliseconds;

    private TemporaryResourceStatus status;
    /**
     * Increments every time the status is changed or progress() is called
     */
    private int resourceVersion;
    private T result;
    private E error;
    private Date expiration;
    private Date timeout;
    private Integer position;
    private Integer maximum;

    protected TemporaryResource(String id, int userId, Long expirationMilliseconds, Long timeoutMilliseconds) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.userId = userId;
        this.expirationMilliseconds = expirationMilliseconds != null && expirationMilliseconds > 0 ? expirationMilliseconds : 0;
        this.timeoutMilliseconds = timeoutMilliseconds;

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
            if (this.timeoutMilliseconds != null && this.timeoutMilliseconds > 0) {
                this.timeout = new Date(Common.timer.currentTimeMillis() + this.timeoutMilliseconds);
            }
            this.startTask();
            if (this.timeout != null) {
                this.scheduleTimeout(this.timeout);
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
            this.expiration = new Date(Common.timer.currentTimeMillis() + this.expirationMilliseconds);
            if (this.expirationMilliseconds == 0) {
                this.removeNow();
            } else {
                this.scheduleRemoval(this.expiration);
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
            this.expiration = new Date(Common.timer.currentTimeMillis() + this.expirationMilliseconds);
            if (this.expirationMilliseconds == 0) {
                this.removeNow();
            } else {
                this.scheduleRemoval(this.expiration);
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
            this.expiration = new Date(Common.timer.currentTimeMillis() + this.expirationMilliseconds);
            if (this.expirationMilliseconds == 0) {
                this.removeNow();
            } else {
                this.scheduleRemoval(this.expiration);
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
            this.expiration = new Date(Common.timer.currentTimeMillis() + this.expirationMilliseconds);
            if (this.expirationMilliseconds == 0) {
                this.removeNow();
            } else {
                this.scheduleRemoval(this.expiration);
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

    public final Date getExpiration() {
        return expiration;
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

    public final Date getTimeout() {
        return timeout;
    }

    public int getResourceVersion() {
        return resourceVersion;
    }

    @Override
    public String toString() {
        return "TemporaryResource [id=" + id + ", userId=" + userId + ", expirationMilliseconds="
                + expirationMilliseconds + ", timeoutMilliseconds=" + timeoutMilliseconds
                + ", status=" + status + ", resourceVersion=" + resourceVersion + ", result="
                + result + ", error=" + error + ", expiration=" + expiration + ", timeout="
                + timeout + ", position=" + position + ", maximum=" + maximum + "]";
    }
}
