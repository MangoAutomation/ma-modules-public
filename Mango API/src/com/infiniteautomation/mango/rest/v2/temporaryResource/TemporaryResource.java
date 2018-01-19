/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.serotonin.m2m2.Common;

/**
 * @author Jared Wiltshire
 * @param <T> result type
 * @param <E> error type
 */
public final class TemporaryResource<T, E> {
    public static enum TemporaryResourceStatus {
        SCHEDULED, RUNNING, TIMED_OUT, CANCELLED, SUCCESS, ERROR;
    }
    
    private final String id;
    private final int userId;
    private final int expirationSeconds;
    private final Runnable cancel;

    private TemporaryResourceStatus status;
    private T result;
    private E error;
    private Date expiration;
    private Integer position;
    private Integer maximum;

    protected TemporaryResource(String id, int userId, int expirationSeconds, Runnable cancel) {
        this.id = id;
        this.userId = userId;
        this.expirationSeconds = expirationSeconds;
        this.cancel = cancel;
        
        this.status = TemporaryResourceStatus.SCHEDULED;
    }

    protected synchronized boolean progress(T result, Integer position, Integer maximum) {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.RUNNING;
            this.result = result;
            this.position = position;
            this.maximum = maximum;
            return true;
        }
        return false;
    }

    protected synchronized boolean timeOut() {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.TIMED_OUT;
            if (this.cancel != null) {
                this.cancel.run();
            }
            this.expiration = new Date(Common.timer.currentTimeMillis() + this.expirationSeconds * 1000);
            return true;
        }
        return false;
    }

    protected synchronized boolean cancel() {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.CANCELLED;
            if (this.cancel != null) {
                this.cancel.run();
            }
            this.expiration = new Date(Common.timer.currentTimeMillis() + this.expirationSeconds * 1000);
            return true;
        }
        return false;
    }

    protected synchronized boolean success(T result) {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.SUCCESS;
            this.result = result;
            this.expiration = new Date(Common.timer.currentTimeMillis() + this.expirationSeconds * 1000);
            return true;
        }
        return false;
    }

    protected synchronized boolean error(E error) {
        if (!this.isComplete()) {
            this.status = TemporaryResourceStatus.ERROR;
            this.error = error;
            this.expiration = new Date(Common.timer.currentTimeMillis() + this.expirationSeconds * 1000);
            return true;
        }
        return false;
    }

    @JsonIgnore
    protected synchronized boolean isComplete() {
        return !(this.status == TemporaryResourceStatus.SCHEDULED || this.status == TemporaryResourceStatus.RUNNING);
    }

    public String getId() {
        return id;
    }

    public TemporaryResourceStatus getStatus() {
        return status;
    }

    public T getResult() {
        return result;
    }

    public E getError() {
        return error;
    }

    public Date getExpiration() {
        return expiration;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getPosition() {
        return position;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getMaximum() {
        return maximum;
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getProgress() {
        if (position != null && maximum != null) {
            return Math.floorDiv(position * 100, maximum);
        }
        return null;
    }

    public int getUserId() {
        return userId;
    }
}
