/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest.temporaryResource;

import java.util.Date;

import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource.StatusUpdateException;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.latest.util.CrudNotificationType;
import com.infiniteautomation.mango.rest.latest.util.RestExceptionMapper;
import com.infiniteautomation.mango.spring.service.PermissionService;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.timeout.HighPriorityTask;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;
import com.serotonin.timer.RejectedTaskReason;
import com.serotonin.timer.TimerTask;

/**
 * @author Jared Wiltshire
 */
public final class MangoTaskTemporaryResourceManager<T> extends TemporaryResourceManager<T, AbstractRestException> implements RestExceptionMapper {

    static class TaskData {
        HighPriorityTask mainTask;
        TimerTask timeoutTask;
        TimerTask expirationTask;
    }

    private final PermissionService permissionService;
    private final TemporaryResourceWebSocketHandler websocketHandler;

    public MangoTaskTemporaryResourceManager(PermissionService permissionService) {
        this(permissionService, null);
    }

    public MangoTaskTemporaryResourceManager(PermissionService permissionService, TemporaryResourceWebSocketHandler websocketHandler) {
        super(permissionService);
        this.websocketHandler = websocketHandler;
        this.permissionService = permissionService;
    }

    @Override
    public void resourceAdded(TemporaryResource<T, AbstractRestException> resource) {
        if (this.websocketHandler != null) {
            this.websocketHandler.notify(CrudNotificationType.CREATE, resource);
        }

        TaskData data = new TaskData();
        resource.setData(data);
    }

    @Override
    public void resourceRemoved(TemporaryResource<T, AbstractRestException> resource) {
        if (this.websocketHandler != null) {
            this.websocketHandler.notify(CrudNotificationType.DELETE, resource);
        }

        TaskData tasks = (TaskData) resource.getData();
        if (tasks.expirationTask != null) {
            tasks.expirationTask.cancel();
        }
    }

    @Override
    public void resourceUpdated(TemporaryResource<T, AbstractRestException> resource) {
        if (this.websocketHandler != null) {
            this.websocketHandler.notify(CrudNotificationType.UPDATE, resource);
        }

        if (resource.getStatus() == TemporaryResourceStatus.SCHEDULED) {
            this.scheduleTask(resource);
        }
    }

    @Override
    public void resourceCompleted(TemporaryResource<T, AbstractRestException> resource) {
        if (this.websocketHandler != null) {
            this.websocketHandler.notify(CrudNotificationType.UPDATE, resource);
        }

        TaskData tasks = (TaskData) resource.getData();
        if (tasks.mainTask != null) {
            tasks.mainTask.cancel();
        }
        if (tasks.timeoutTask != null) {
            tasks.timeoutTask.cancel();
        }
        this.scheduleRemoval(resource);
    }

    private void scheduleTask(TemporaryResource<T, AbstractRestException> resource) {
        TaskData tasks = (TaskData) resource.getData();

        tasks.mainTask = new HighPriorityTask("Temporary resource " + resource.getResourceType() + " " + resource.getId()) {
            @Override
            public void run(long runtime) {
                permissionService.runAs(resource.getUser(), ()-> {
                    try {
                        resource.runTask();
                    } catch (Exception e) {
                        AbstractRestException error = MangoTaskTemporaryResourceManager.this.mapException(e);
                        resource.safeError(error);
                    }
                });
            }

            @Override
            public void rejected(RejectedTaskReason reason) {
                super.rejected(reason);

                TranslatableMessage msg = null;
                switch (reason.getCode()) {
                    case RejectedTaskReason.POOL_FULL:
                        msg = new TranslatableMessage("rest.error.rejectedTaskPoolFull");
                        break;
                    case RejectedTaskReason.TASK_QUEUE_FULL:
                        msg = new TranslatableMessage("rest.error.rejectedTaskQueueFull");
                        break;
                    case RejectedTaskReason.CURRENTLY_RUNNING:
                        msg = new TranslatableMessage("rest.error.rejectedTaskAlreadyRunning");
                        break;
                }

                ServerErrorException ex = msg == null ? new ServerErrorException() : new ServerErrorException(msg);
                AbstractRestException error = MangoTaskTemporaryResourceManager.this.mapException(ex);
                resource.safeError(error);
            }
        };

        Common.backgroundProcessing.execute(tasks.mainTask);
        this.scheduleTimeout(resource);
    }

    private void scheduleTimeout(TemporaryResource<T, AbstractRestException> resource) {
        if (resource.getTimeout() <= 0) return;

        TaskData tasks = (TaskData) resource.getData();
        Date timeoutDate = new Date(resource.getStartTime().getTime() + resource.getTimeout());

        // TimeoutTask schedules itself to be executed
        tasks.timeoutTask = new TimeoutTask(timeoutDate, new TimeoutClient() {
            @Override
            public void scheduleTimeout(long fireTime) {
                try {
                    resource.timeOut();
                } catch (StatusUpdateException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Tried to time out resource but it was already complete", e);
                    }
                }
            }

            @Override
            public String getThreadName() {
                return "Temporary resource timeout " + resource.getResourceType() + " " + resource.getId();
            }

            @Override
            public void rejected(RejectedTaskReason reason) {
                super.rejected(reason);
                try{
                    resource.timeOut();
                } catch (StatusUpdateException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Tried to time out resource but it was already complete", e);
                    }
                }
            }
        });
    }

    private void scheduleRemoval(TemporaryResource<T, AbstractRestException> resource) {
        if (resource.getExpiration() <= 0) {
            resource.remove();
            return;
        }

        TaskData tasks = (TaskData) resource.getData();
        Date expirationDate = new Date(resource.getCompletionTime().getTime() + resource.getExpiration());

        // TimeoutTask schedules itself to be executed
        tasks.expirationTask = new TimeoutTask(expirationDate, new TimeoutClient() {
            @Override
            public void scheduleTimeout(long fireTime) {
                try {
                    resource.remove();
                } catch (StatusUpdateException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Tried to remove resource but it was already complete", e);
                    }
                }
            }

            @Override
            public String getThreadName() {
                return "Temporary resource expiration " + resource.getResourceType() + " " + resource.getId();
            }

            @Override
            public void rejected(RejectedTaskReason reason) {
                super.rejected(reason);
                try{
                    resource.remove();
                } catch (StatusUpdateException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Tried to remove resource but it was already complete", e);
                    }
                }
            }
        });
    }
}
