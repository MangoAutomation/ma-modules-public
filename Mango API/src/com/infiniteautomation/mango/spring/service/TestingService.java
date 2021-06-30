/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 *
 *
 */

package com.infiniteautomation.mango.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.infiniteautomation.mango.spring.ConditionalOnProperty;
import com.serotonin.m2m2.Common;

@Service
@ConditionalOnProperty(value = {"${testing.enabled:false}"})
public class TestingService {

    private final ApplicationEventPublisher eventPublisher;
    private final PermissionService permissionService;

    @Autowired
    public TestingService(final ApplicationEventPublisher eventPublisher, final PermissionService permissionService) {
        this.eventPublisher = eventPublisher;
        this.permissionService = permissionService;
    }
    /**
     * Generate an event that can be run upon receipt
     * Must be superadmin
     */
    public void generateRunnableEvent(Runnable runnable) {
        this.permissionService.ensureAdminRole(Common.getUser());
        this.eventPublisher.publishEvent(new RunnableTestEvent(this, runnable));
    }

    public class RunnableTestEvent extends ApplicationEvent {
        final Runnable runnable;
        public RunnableTestEvent(Object source, Runnable runnable) {
            super(source);
            this.runnable = runnable;
        }
        public void run() {
            this.runnable.run();
        }
    }
}
