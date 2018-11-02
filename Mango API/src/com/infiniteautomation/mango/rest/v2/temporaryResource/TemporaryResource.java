/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResourceManager.ResourceTask;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.User;

/**
 * @author Jared Wiltshire
 * @param <T> result type
 * @param <E> error type
 */
public final class TemporaryResource<T, E> {
    public static enum TemporaryResourceStatus {
        VIRGIN, SCHEDULED, RUNNING, TIMED_OUT, CANCELLED, SUCCESS, ERROR;
    }

    public static class StatusUpdateException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final String resourceType;
    private final String id;
    private final int userId;
    private final long expiration;
    private final long timeout;
    private final ResourceTask<T, E> task;
    private final TemporaryResourceManager<T, E> manager;

    private TemporaryResourceStatus status;
    /**
     * Increments every time the status is changed or progress() is called
     */
    private int resourceVersion;
    @JsonView(TemporaryResourceViews.ShowResult.class)
    private T result;
    private E error;
    private Date startTime;
    private Date completionTime;
    private Integer position;
    private Integer maximum;
    private Consumer<TemporaryResource<T, E>> cancelCallback;

    /**
     * Holds runtime data for the resource manager to use
     */
    private Object data;

    /**
     * @param resourceType unique type string assigned to each resource type e.g. BULK_DATA_POINT
     * @param id if null will be assigned a UUID
     * @param userId user id of the user that started the temporary resource
     * @param expiration time after the resource completes that it will be removed (milliseconds)
     * @param timeout time after the resource starts that it will be timeout if not complete (milliseconds)
     * @param task the task to run
     * @param manager the resource manager to which this resource belongs
     */
    protected TemporaryResource(String resourceType, String id, int userId, long expiration, long timeout, ResourceTask<T, E> task, TemporaryResourceManager<T, E> manager) {
        this.task = task;
        this.manager = manager;
        this.resourceType = resourceType;
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.userId = userId;
        this.expiration = expiration;
        this.timeout = timeout;

        this.status = TemporaryResourceStatus.VIRGIN;
        this.resourceVersion = 0;
    }

    protected synchronized final void schedule() {
        if (this.status != TemporaryResourceStatus.VIRGIN) throw new StatusUpdateException();

        this.status = TemporaryResourceStatus.SCHEDULED;
        this.resourceVersion++;
        this.startTime = new Date(Common.timer.currentTimeMillis());
        this.manager.resourceUpdated(this);
    }

    public synchronized final void progress(T result, Integer position, Integer maximum) {
        if (this.isComplete()) throw new StatusUpdateException();

        this.status = TemporaryResourceStatus.RUNNING;
        this.resourceVersion++;
        this.result = result;
        this.position = position;
        this.maximum = maximum;
        this.manager.resourceUpdated(this);
    }

    public synchronized final void progressOrSuccess(T result, Integer position, Integer maximum) {
        if (this.isComplete()) throw new StatusUpdateException();

        this.resourceVersion++;
        this.result = result;
        this.position = position;
        this.maximum = maximum;

        if (position != null && position.equals(maximum)) {
            this.status = TemporaryResourceStatus.SUCCESS;
            this.completionTime = new Date(Common.timer.currentTimeMillis());
            this.manager.resourceCompleted(this);
        } else {
            this.status = TemporaryResourceStatus.RUNNING;
            this.manager.resourceUpdated(this);
        }
    }

    protected synchronized final void timeOut() {
        if (this.isComplete()) throw new StatusUpdateException();

        this.status = TemporaryResourceStatus.TIMED_OUT;
        this.resourceVersion++;
        this.completionTime = new Date(Common.timer.currentTimeMillis());
        this.manager.resourceCompleted(this);
    }

    public synchronized final void cancel() {
        if (this.isComplete()) throw new StatusUpdateException();

        this.status = TemporaryResourceStatus.CANCELLED;
        this.resourceVersion++;
        this.completionTime = new Date(Common.timer.currentTimeMillis());
        this.manager.resourceCompleted(this);

        if (this.cancelCallback != null) {
            this.cancelCallback.accept(this);
        }
    }

    public synchronized final void success(T result) {
        if (this.isComplete()) throw new StatusUpdateException();

        this.status = TemporaryResourceStatus.SUCCESS;
        this.resourceVersion++;
        this.completionTime = new Date(Common.timer.currentTimeMillis());
        this.manager.resourceCompleted(this);
    }

    public synchronized final void error(E error) {
        if (this.isComplete()) throw new StatusUpdateException();

        this.status = TemporaryResourceStatus.ERROR;
        this.resourceVersion++;
        this.completionTime = new Date(Common.timer.currentTimeMillis());
        this.manager.resourceCompleted(this);
    }

    protected synchronized final void safeError(E error) {
        if (this.isComplete()) return;
        this.error(error);
    }

    public synchronized final void remove() {
        if (!this.isComplete()) throw new StatusUpdateException();
        this.manager.remove(this);
    }

    @JsonIgnore
    public synchronized final boolean isComplete() {
        return !(this.status == TemporaryResourceStatus.VIRGIN || this.status == TemporaryResourceStatus.SCHEDULED || this.status == TemporaryResourceStatus.RUNNING);
    }

    @JsonIgnore
    protected final void runTask(User user) throws Exception {
        this.cancelCallback = task.run(this, user);
    }

    @JsonIgnore
    protected final void setData(Object data) {
        this.data = data;
    }

    @JsonIgnore
    protected final Object getData() {
        return data;
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

    public final int getResourceVersion() {
        return resourceVersion;
    }

    public final String getResourceType() {
        return resourceType;
    }

    public final long getExpiration() {
        return expiration;
    }

    public final long getTimeout() {
        return timeout;
    }

    public final Date getStartTime() {
        return startTime;
    }

    public final Date getCompletionTime() {
        return completionTime;
    }

    @Override
    public String toString() {
        return "TemporaryResource [resourceType=" + resourceType + ", id=" + id + ", userId="
                + userId + ", expiration=" + expiration + ", timeout=" + timeout + ", status="
                + status + ", resourceVersion=" + resourceVersion + ", result=" + result
                + ", error=" + error + ", startTime=" + startTime + ", completionTime="
                + completionTime + ", position=" + position + ", maximum=" + maximum + "]";
    }

}
