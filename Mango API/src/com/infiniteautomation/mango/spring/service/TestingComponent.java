/*
 * Copyright (C) 2021 Radix IoT LLC. All rights reserved.
 *
 *
 */

package com.infiniteautomation.mango.spring.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.infiniteautomation.mango.spring.ConditionalOnProperty;

@Component
@ConditionalOnProperty(value = {"${testing.enabled:false}"})
public class TestingComponent {

    /**
     * Listen for and run any Runnable Event
     * @param event
     */
    @EventListener
    protected void handleRunnableEvent(TestingService.RunnableTestEvent event) {
        event.run();
    }
}
