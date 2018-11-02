/*
 * Copyright (C) 2018 Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.temporaryResource;

import java.util.Date;

import com.infiniteautomation.mango.rest.v2.exception.AbstractRestV2Exception;
import com.infiniteautomation.mango.rest.v2.exception.AccessDeniedException;
import com.infiniteautomation.mango.rest.v2.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.v2.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.v2.util.CrudNotificationType;
import com.infiniteautomation.mango.rest.v2.util.RestExceptionMapper;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.BackgroundContext;
import com.serotonin.m2m2.util.timeout.HighPriorityTask;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;
import com.serotonin.m2m2.vo.User;
import com.serotonin.timer.RejectedTaskReason;
import com.serotonin.timer.TimerTask;

/**
 * @author Jared Wiltshire
 */
public final class MangoTaskTemporaryResourceManager<T> extends TemporaryResourceManager<T, AbstractRestV2Exception> implements RestExceptionMapper {

    static class TaskData {
        HighPriorityTask mainTask;
        TimerTask timeoutTask;
        TimerTask expirationTask;
    }

    private final TemporaryResourceWebSocketHandler websocketHandler;

    public MangoTaskTemporaryResourceManager() {
        this(null);
    }

    public MangoTaskTemporaryResourceManager(TemporaryResourceWebSocketHandler websocketHandler) {
        this.websocketHandler = websocketHandler;
    }

    @Override
    public void resourceAdded(TemporaryResource<T, AbstractRestV2Exception> resource) {
        if (this.websocketHandler != null) {
            this.websocketHandler.notify(CrudNotificationType.CREATE, resource);
        }

        TaskData data = new TaskData();
        resource.setData(data);
    }

    @Override
    public void resourceRemoved(TemporaryResource<T, AbstractRestV2Exception> resource) {
        if (this.websocketHandler != null) {
            this.websocketHandler.notify(CrudNotificationType.DELETE, resource);
        }

        TaskData tasks = (TaskData) resource.getData();
        if (tasks.expirationTask != null) {
            tasks.expirationTask.cancel();
        }
    }

    @Override
    public void resourceUpdated(TemporaryResource<T, AbstractRestV2Exception> resource) {
        if (this.websocketHandler != null) {
            this.websocketHandler.notify(CrudNotificationType.UPDATE, resource);
        }

        if (resource.getStatus() == TemporaryResourceStatus.SCHEDULED) {
            this.scheduleTask(resource);
        }
    }

    @Override
    public void resourceCompleted(TemporaryResource<T, AbstractRestV2Exception> resource) {
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

    private void scheduleTask(TemporaryResource<T, AbstractRestV2Exception> resource) {
        TaskData tasks = (TaskData) resource.getData();

        User user = UserDao.getInstance().get(resource.getUserId());
        // user might have been deleted since task was scheduled
        if (user == null) {
            AccessDeniedException error = new AccessDeniedException();
            resource.safeError(error);
            return;
        }

        tasks.mainTask = new HighPriorityTask("Temporary resource " + resource.getResourceType() + " " + resource.getId()) {
            @Override
            public void run(long runtime) {
                try {
                    BackgroundContext.set(user);
                    resource.runTask(user);
                } catch (Exception e) {
                    AbstractRestV2Exception error = MangoTaskTemporaryResourceManager.this.mapException(e);
                    resource.safeError(error);
                } finally {
                    BackgroundContext.remove();
                }
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
                AbstractRestV2Exception error = MangoTaskTemporaryResourceManager.this.mapException(ex);
                resource.safeError(error);
            }
        };

        Common.backgroundProcessing.execute(tasks.mainTask);
        this.scheduleTimeout(resource);
    }

    private void scheduleTimeout(TemporaryResource<T, AbstractRestV2Exception> resource) {
        if (resource.getTimeout() <= 0) return;

        TaskData tasks = (TaskData) resource.getData();
        Date timeoutDate = new Date(resource.getStartTime().getTime() + resource.getTimeout());

        // TimeoutTask schedules itself to be executed
        tasks.timeoutTask = new TimeoutTask(timeoutDate, new TimeoutClient() {
            @Override
            public void scheduleTimeout(long fireTime) {
                resource.timeOut();
            }

            @Override
            public String getThreadName() {
                return "Temporary resource timeout " + resource.getResourceType() + " " + resource.getId();
            }

            @Override
            public void rejected(RejectedTaskReason reason) {
                super.rejected(reason);
                resource.timeOut();
            }
        });
    }

    private void scheduleRemoval(TemporaryResource<T, AbstractRestV2Exception> resource) {
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
                resource.remove();
            }

            @Override
            public String getThreadName() {
                return "Temporary resource expiration " + resource.getResourceType() + " " + resource.getId();
            }

            @Override
            public void rejected(RejectedTaskReason reason) {
                super.rejected(reason);
                resource.remove();
            }
        });
    }
}
