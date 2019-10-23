/**
 * Copyright (C) 2018  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mangoApi.rootRest;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * The common REST Root Context
 *
 * @author Terry Packer
 *
 */
@Configuration
@EnableWebMvc
public class RootRestDispatcherConfiguration {
    public static final String CONTEXT_ID = "restDispatcherContext";
    public static final String DISPATCHER_NAME = "REST_DISPATCHER";
}
